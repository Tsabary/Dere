package co.getdere.Fragments


import android.Manifest
import android.os.Bundle
import android.app.Fragment
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup

import co.getdere.R
import kotlinx.android.synthetic.main.fragment_preview.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class PreviewFragment : Fragment() {

    private lateinit var backgroundThread: HandlerThread

    private lateinit var backgroundHandler: Handler

    private val cameraManager by lazy {
        activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val surfaceListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.d(TAG, "texturesurface width: $width height: $height")
            openCamera()
        }

    }

    private lateinit var cameraDevice : CameraDevice

    private val deviceStateCallback = object : CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "Camera device opened successfully")
            cameraDevice = camera
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "Camera device disconnected")
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(TAG, "Camera device error")
            this@PreviewFragment.activity.finish()
        }

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }


    override fun onResume() {
        super.onResume()

        startBackgroundThread()
        if (preview_texture_view.isAvailable) {
            openCamera()
        } else {
            preview_texture_view.surfaceTextureListener = surfaceListener
        }
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }


    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
        private val TAG = PreviewFragment::class.qualifiedName
        @JvmStatic
        fun newInstance() = PreviewFragment()
    }


    private fun startBackgroundThread() {

        backgroundThread = HandlerThread("Camera2 kotlin").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun cameraId(lens: Int): String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING) }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

        return deviceId[0]
    }

    private fun connectCamera() {
        val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK)
        Log.d(TAG, "deviceId: $deviceId")
        try {
            cameraManager.openCamera(deviceId,deviceStateCallback,backgroundHandler)
        } catch (e: CameraAccessException) {
        Log.e(TAG, e.toString())
        } catch (e: InterruptedException){
            Log.e(TAG, "Open camera device interrupted while opened")
        }
    }


    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>): T? {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> {
                characteristics.get(key)
            }
            else -> throw IllegalArgumentException("key not recognized")
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    private fun checkCameraPermissions() {
        if (EasyPermissions.hasPermissions(activity!!, Manifest.permission.CAMERA)) {
            Log.d(TAG, "App has camera permissions")
            connectCamera()
        } else {
            EasyPermissions.requestPermissions(
                activity!!,
                "Camera app requires camera access",
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
        }
    }


    private fun openCamera() {
        checkCameraPermissions()
    }


}
