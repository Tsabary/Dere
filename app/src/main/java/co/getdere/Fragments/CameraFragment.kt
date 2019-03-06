package co.getdere.Fragments


import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*

import co.getdere.R

class CameraFragment : Fragment(), SurfaceView(), SurfaceHolder.Callback {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        getCameraInstance()

        return inflater.inflate(R.layout.fragment_camera, container, false)

    }

    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }


}
