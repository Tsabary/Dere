package co.getdere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import co.getdere.adapters.RegisterLoginPagerAdapter
import kotlinx.android.synthetic.main.activity_register_login.*

class RegisterLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_login)

        val viewPager = register_login_viewpager
        val pagerAdapter = RegisterLoginPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter
        val tabLayout = register_login_tablayout
        tabLayout.setupWithViewPager(viewPager)
    }
}
