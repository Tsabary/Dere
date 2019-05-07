package co.getdere

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import co.getdere.adapters.OnBoardingPagerAdapter
import kotlinx.android.synthetic.main.activity_on_boarding.*

class OnBoardingActivity : AppCompatActivity() {

    val title1 = "Welcome to Dere"
    val subTitle1 = "All your future travel destinations in one place, sorted by your interests"

    val title2 = "Photos"
    val subTitle2 = "Are all geo-tagged, so saving a photo adds it to your map"

    val title3 = "Buckets"
    val subTitle3 =
        "Are like binders for your dreams. Save different photos to different buckets according to your interests"

    val title4 = "Board"
    val subTitle4 = "Is where you talk to other explorers. Ask questions and share your knowledge"

    var viewPagerPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        val viewPager = on_boarding_viewpager
        val pagerAdapter = OnBoardingPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        val button = on_boarding_button

        val title = on_boarding_title
        val subTitle = on_boarding_subtitle

        button.setOnClickListener {
            when (viewPagerPosition) {
                0 -> {
                    title.text = title2
                    subTitle.text = subTitle2
                    viewPager.currentItem = 1
                    viewPagerPosition = 1
                }

                1 -> {
                    title.text = title3
                    subTitle.text = subTitle3
                    viewPager.currentItem = 2
                    viewPagerPosition = 2

                }

                2 -> {
                    title.text = title4
                    subTitle.text = subTitle4
                    viewPager.currentItem = 3
                    viewPagerPosition = 3
                }

                3 -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }

    }

}