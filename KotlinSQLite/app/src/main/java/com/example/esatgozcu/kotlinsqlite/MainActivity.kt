package com.example.esatgozcu.kotlinsqlite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    var selectedImage : Bitmap? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        // menuInflater oluşturup menümüzle bağlıyoruz.
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_movie,menu)

        return super.onCreateOptionsMenu(menu)
    }
    // Menüden herhangi bir elemana tıkladığımızda
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        // Menüden film ekleye tıkladığımızda..
        if (item?.itemId == R.id.add_movie) {

            // Verileri sıfırlıyoruz
            movieName.setText("")
            movieYear.setText("")
            movieImdb.setText("")
            // Arka plan resmini sıfırlıyoruz
            val background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.add_image);
            imageView.setImageBitmap(background)

            secondLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getData()

    }

    fun getData()
    {

        // Verilerimiz için ArrayListlerimizi oluşturuyoruz.
        val movieNameArray = ArrayList<String>()
        val movieYearArray = ArrayList<String>()
        val movieImdbArray = ArrayList<String>()
        val movieImageArray = ArrayList<Bitmap>()

        // ArrayListleri listView'e aktarmak için adapter oluşturuyoruz
        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,movieNameArray)
        // ListView ile adapteri bağlıyoruz
        listView.adapter = arrayAdapter

        try {
            /*
                Verilerimizi veritabanından çekip arraylistlere aktarıyoruz
             */

            // Movies isminde yeni veritabanı oluşturuyoruz.
            val database = this.openOrCreateDatabase("Movies", Context.MODE_PRIVATE,null)
            // Veritabanımızın için movies isimli tablomuzu oluturuyoruz.
            database.execSQL("CREATE TABLE IF NOT EXISTS movies (name VARCHAR,year VARCHAR,imdb VARCHAR,image BLOB)")
            // Bütün verileri çekiyoruz ve cursor nesnesine aktarıyoruz index numarasına göre verileri tekrardan çekeceğiz
            val cursor = database.rawQuery("SELECT * FROM movies",null)

            // Verileri çekebilmek için index numaralarını alıyoruz.
            val nameIx = cursor.getColumnIndex("name")
            val yearIx = cursor.getColumnIndex("year")
            val imdbIx = cursor.getColumnIndex("imdb")
            val imageIx = cursor.getColumnIndex("image")

            // Satır başına gidiyoruz.
            cursor.moveToFirst()

            while (cursor!=null)
            {
                // cursor nesnesi boş değilse verilerimizi ArrayListlere aktarıyoruz.
                movieNameArray.add(cursor.getString(nameIx))
                movieYearArray.add(cursor.getString(yearIx))
                movieImdbArray.add(cursor.getString(imdbIx))

                // Resimleri ekliyoruz.
                val byteArray = cursor.getBlob(imageIx)
                val image = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                movieImageArray.add(image)

                // Bir sonraki satıra geçiyoruz.
                cursor.moveToNext()

                // ArrayAdaptere değişiklik olduğunu bildiriyoruz.
                arrayAdapter.notifyDataSetChanged()
            }

            cursor?.close()

        }catch (e:Exception)
        {
            // Herhangi bir hata ile karşılaşıldığı zaman hatayı bastırıyoruz.
            e.printStackTrace()
        }

        // ListView'den herhangi bir item seçildiği zaman..
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->

            // Seçilen filmin verilerini aktarıyoruz.
            movieName.setText(movieNameArray.get(position))
            movieYear.setText(movieYearArray.get(position))
            movieImdb.setText(movieImdbArray.get(position))
            imageView.setImageBitmap(movieImageArray.get(position))

            listView.setVisibility(View.INVISIBLE)
            secondLayout.setVisibility(View.VISIBLE)
        }
    }
    // ImageView üstüne tıklanıp resim seçilmek istendiğinde..
    fun select(view:View)
    {
        // Gerekli izinleri alınıp alınmadığını kontrol ediyoruz.
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // İzin alınmamış ise
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),2)
        } else {

            // İzin alındı ise resim galerisine gidiyoruz
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode==2)
        {
            // İzin sonucunda izin verilip verilmediğini tekrardan kontrol ediyoruz
            if (grantResults.size >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,1)
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Resim galerisinden resim seçildi ise ve data boş değil ise ...
        if (requestCode ==1 && resultCode == Activity.RESULT_OK && data!=null)
        {
            val image = data.data

            try {

                // Seçilen resimi ImageView'e aktarıyoruz
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver,image)
                imageView.setImageBitmap(selectedImage)

            }catch (e:Exception)
            {
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun save (view: View)
    {
        val valMovieName = movieName.text.toString()
        val valMovieYear= movieYear.text.toString()
        val valMovieImdb = movieImdb.text.toString()

        val outputStream = ByteArrayOutputStream()
        selectedImage?.compress(Bitmap.CompressFormat.PNG,50,outputStream)
        val byteArray = outputStream.toByteArray()

        try {

            // Movies isminde yeni veritabanı oluşturuyoruz.
            val database = this.openOrCreateDatabase("Movies", Context.MODE_PRIVATE,null)
            // Veritabanımızın için movies isimli tablomuzu oluturuyoruz.
            database.execSQL("CREATE TABLE IF NOT EXISTS movies (name VARCHAR,year VARCHAR,imdb VARCHAR,image BLOB)")

            // Sql sorgumuzu hazırlıyoruz
            val sqlString = "INSERT INTO movies (name,year,imdb,image) VALUES (?, ?, ?, ?)"
            val statement = database.compileStatement(sqlString)

            // Verilerimizi ekliyoruz
            statement.bindString(1,valMovieName)
            statement.bindString(2,valMovieYear)
            statement.bindString(3,valMovieImdb)
            statement.bindBlob(4,byteArray)
            statement.execute()

            // Eklenilen verilerin güncellenmesi için getData fonksiyonunu tekrar çağırıyoruz.
            getData()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        listView.setVisibility(View.VISIBLE)
        secondLayout.setVisibility(View.INVISIBLE)
    }

    fun back (view: View)
    {
        listView.setVisibility(View.VISIBLE)
        secondLayout.setVisibility(View.INVISIBLE)
    }
}
