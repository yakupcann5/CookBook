package com.example.kotlinexamplesixteen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream
import java.lang.Exception


class TarifFragment : Fragment() {
    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            kaydet(it)
        }
        imageView2.setOnClickListener{
            gorselSec(it)
        }
        arguments?.let {
            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (gelenBilgi.equals("menudengeldim")){
                //yeni yemek
                yemekAdiText.setText("")
                malzemeAdiText.setText("")
                button.visibility = View.VISIBLE
                val gorselSecmeArkaPlan = BitmapFactory.decodeResource(context?.resources,R.drawable.adsz)
                imageView2.setImageBitmap(gorselSecmeArkaPlan)
            }else{
                //eski yemek
                button.visibility = View.INVISIBLE
                val secilenId = TarifFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()) {

                            yemekAdiText.setText(cursor.getString(yemekIsmiIndex))
                            malzemeAdiText.setText(cursor.getString(yemekMalzemeIndex))
                            val byteDizisi = cursor.getBlob(yemekGorseli)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            imageView2.setImageBitmap(bitmap)
                        }
                        cursor.close()
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    fun kaydet ( view : View){
        //SQLite'a kaydetme
        val yemekİsmi=yemekAdiText.text.toString()
        val yemekMalzemeleri=malzemeAdiText.text.toString()
        if (secilenBitmap != null){
            val kucukBitmap = bitmapKucult(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap!!.compress(Bitmap.CompressFormat.PNG,50,outputStream)
                val byteDizisi = outputStream.toByteArray()
                try {
                    context?.let {
                        val dataBase =
                            it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                        dataBase.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY AUTOINCREMENT,yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB )")

                        val sqlString="INSERT INTO yemekler (yemekismi,yemekmalzemesi,gorsel)VALUES(?,?,?)"
                        val statement = dataBase.compileStatement(sqlString)
                        statement.bindString(1,yemekİsmi)
                        statement.bindString(2,yemekMalzemeleri)
                        statement.bindBlob(3,byteDizisi)
                        statement.execute()
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
                val action = TarifFragmentDirections.actionTarifFragmentToListFragment()
                Navigation.findNavController(view).navigate(action)
        }

    }
    fun gorselSec(view: View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){
            if (grantResults.size>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode==Activity.RESULT_OK && data!=null){
            secilenGorsel=data.data
            try {
                context?.let{
                    if (secilenGorsel!=null){
                        if (Build.VERSION.SDK_INT>=28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap=ImageDecoder.decodeBitmap(source)
                            imageView2.setImageBitmap(secilenBitmap)
                        }else{
                            secilenBitmap=MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            imageView2.setImageBitmap(secilenBitmap)
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    fun bitmapKucult(kullanıcıBitmapi : Bitmap,maxBoyut : Int): Bitmap? {
        var en = kullanıcıBitmapi.width
        var boy = kullanıcıBitmapi.height
        val bitmapOran : Double = en.toDouble()/boy.toDouble()
        if (bitmapOran>1){
            //yatay
            en=maxBoyut
            val yeniEn = en/bitmapOran
            boy= yeniEn.toInt()
        }else{
            //dikey
            boy = maxBoyut
            val yeniBoy= boy*bitmapOran
            en = yeniBoy.toInt()
        }
        return Bitmap.createScaledBitmap(kullanıcıBitmapi,en,boy,true)
    }
}