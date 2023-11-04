package com.example.chitchat.pagerLayout

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chitchat.friendList.FriendListFragment
import com.example.chitchat.friendList.InviteFragment

class FragmentClass(fm : FragmentManager , lifecycle: Lifecycle) :
FragmentStateAdapter(fm , lifecycle){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        if (position == 1){
            return InviteFragment()
        }
        return FriendListFragment()
    }
}