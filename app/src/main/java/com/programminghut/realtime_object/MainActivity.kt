package com.programminghut.realtime_object

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.programminghut.realtime_object.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.LinkedList


class MainActivity : AppCompatActivity() {

    lateinit var labels:List<String>
    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED)
    val paint = Paint()
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap:Bitmap
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var model:SsdMobilenetV11Metadata1
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var trafficSignSoundPlayer: MediaPlayer
    var previousAreas = mutableMapOf<Float, Float>() // Maps classes to their previous areas
    var recentAreas = mutableMapOf<Float, LinkedList<Float>>() // Maps classes to their recent areas
    var recentIncreases = mutableMapOf<Float, LinkedList<Boolean>>() // Maps classes to their recent increases



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        get_permission()

        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = SsdMobilenetV11Metadata1.newInstance(this)
        trafficSignSoundPlayer = MediaPlayer.create(this, R.raw.stop)
        mediaPlayer = MediaPlayer.create(this, R.raw.colwar)
        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)


        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera()
            }
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = textureView.bitmap!!
                var image = TensorImage.fromBitmap(bitmap)
                image = imageProcessor.process(image)

                val outputs = model.process(image)
                val locations = outputs.locationsAsTensorBuffer.floatArray
                val classes = outputs.classesAsTensorBuffer.floatArray
                val scores = outputs.scoresAsTensorBuffer.floatArray

                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutable)

                val h = mutable.height
                val w = mutable.width
                paint.textSize = h/15f
                paint.strokeWidth = h/85f
                var x = 0
                val allowedClasses = listOf(0f, 1f, 2f, 3f, 7f, 9f)
                val trafficSignClass = 14f
                val alertThreshold = 0.7f
                val minConsecutiveFrames = 7

                scores.forEachIndexed { index, fl ->
                    x = index
                    x *= 4
                    if(fl > 0.6 && allowedClasses.contains(classes[index])){
                        val left = locations[x+1]*w
                        val top = locations[x]*h
                        val right = locations[x+3]*w
                        val bottom = locations[x+2]*h
                        val area = (right - left) * (bottom - top)

                        val recentAreaList = recentAreas.getOrPut(classes[index]) { LinkedList() }
                        recentAreaList.addLast(area)


                        if (recentAreaList.size > minConsecutiveFrames) {
                            recentAreaList.removeFirst()
                        }

                        val avgArea = recentAreaList.average().toFloat()

                        if(classes[index] == trafficSignClass) {
                            if(!trafficSignSoundPlayer.isPlaying) {
                                trafficSignSoundPlayer.start()
                            }
                        } else if (allowedClasses.contains(classes[index])) {
                            val recentIncreaseList = recentIncreases.getOrPut(classes[index]) { LinkedList() }

                            val areaChange = area - avgArea

                            recentIncreaseList.addLast(areaChange > alertThreshold)

                            if (recentIncreaseList.size > minConsecutiveFrames) {
                                recentIncreaseList.removeFirst()
                            }

                            if (recentIncreaseList.all { it } && !mediaPlayer.isPlaying) {
                                mediaPlayer.start()
                            }
                        }

                        previousAreas[classes[index]] = area

                        paint.setColor(colors[index])
                        paint.style = Paint.Style.STROKE
                        canvas.drawRect(RectF(left, top, right, bottom), paint)
                        paint.style = Paint.Style.FILL
                        canvas.drawText(labels[classes[index].toInt()]+" "+fl.toString(), left, top, paint)
                    }
                }

                imageView.setImageBitmap(mutable)
            }






        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
        if(mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null) // This will remove all callbacks and messages from the handler
    }


    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object:CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                var surfaceTexture = textureView.surfaceTexture
                var surface = Surface(surfaceTexture)

                var captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }
                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                    }
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }
        }, handler)
    }

    fun get_permission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
            get_permission()
        }
    }
}