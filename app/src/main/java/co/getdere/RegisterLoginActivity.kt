package co.getdere

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.getdere.adapters.RegisterLoginPagerAdapter
import kotlinx.android.synthetic.main.activity_register_login.*

class RegisterLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_login)

        val viewPager = register_login_viewpager
        viewPager.adapter = RegisterLoginPagerAdapter(supportFragmentManager)
        val tabLayout = register_login_tablayout
        tabLayout.setupWithViewPager(viewPager)
    }
}
