package com.example.chitchat.friendList

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.chitchat.databinding.ActivityForFragmentsBinding
import com.example.chitchat.pagerLayout.FragmentClass
import com.google.android.material.tabs.TabLayout

class ActivityForFragments : AppCompatActivity() {

    private lateinit var binding: ActivityForFragmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForFragmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.tabLayout .addTab(binding.tabLayout.newTab().setText("FriendList"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Invite"))

        val fragmentManager = supportFragmentManager

        val adapter = FragmentClass(fragmentManager , lifecycle)
        binding.viewPager2.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object :
        TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null){
                    binding.viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        binding.viewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }
}