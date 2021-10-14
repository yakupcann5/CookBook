package com.example.kotlinexamplesixteen

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import java.lang.Exception


class ListFragment : Fragment() {
    var yemekIsmiListesi = ArrayList<String>()
    var yemekIdListesi=ArrayList<Int>()
    private lateinit var listeAdapter : ListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeAdapter= ListRecyclerAdapter(yemekIsmiListesi,yemekIdListesi)
        recyclerView.layoutManager=LinearLayoutManager(context)
        recyclerView.adapter=listeAdapter

        sqlVeriALma()
    }
    fun sqlVeriALma(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM yemekler", null)
                val yemekIsmiIndex=cursor.getColumnIndex("yemekismi")
                val yemekIdIndex = cursor.getColumnIndex("id")
                yemekIsmiListesi.clear()
                yemekIdListesi.clear()
                while (cursor.moveToNext()) {
                    yemekIsmiListesi.add(cursor.getString(yemekIsmiIndex))
                    yemekIdListesi.add(cursor.getInt(yemekIdIndex))

                }
                listeAdapter.notifyDataSetChanged()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
