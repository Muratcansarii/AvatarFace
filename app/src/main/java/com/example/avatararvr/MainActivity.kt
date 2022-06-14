package com.example.avatararvr

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.*
import android.util.Base64.encodeToString
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.avatararvr.model.PostZipSetterGetter
import com.example.avatararvr.model.PostZipSetterGetterInput
import com.example.avatararvr.retrofit.RetrofitClient
import com.example.avatararvr.viewmodel.MainActivityViewModel
import ir.alirezabdn.wp7progress.WP7ProgressBar
import okhttp3.Credentials
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class MainActivity : AppCompatActivity() {

    lateinit var context: Context
    val downloadStatus = MutableLiveData<Boolean>()
    lateinit var mainActivityViewModel: MainActivityViewModel
    lateinit var webView: WebView
    lateinit var wp7progressBar: WP7ProgressBar
    private var downloadStarted = false
    private var serviceStarted = false
    private val PermissionsRequestCode = 123
    private lateinit var managePermissions: ManagePermissions
    //var requiredPermissions = arrayOf<String>(Permissions.CAMERA, Permissions.WRITE_EXTERNAL_STORAGE, Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_SETTINGS)

    val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1
    var uploadMessage: ValueCallback<Array<Uri>>? = null

    var link: String? = null
    private var mUploadMessage: ValueCallback<*>? = null

    private fun setupPermissions() {
        val list = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS
        )

        // Initialize a new instance of ManagePermissions class
        managePermissions = ManagePermissions(this, list, PermissionsRequestCode)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            managePermissions.checkPermissions()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setupPermissions()

        context = this@MainActivity

        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        getToken()


        val btnClick = findViewById<Button>(R.id.btnClick)
        val btnCreateAvatar = findViewById<Button>(R.id.btnCreateAvatarClick)
        val btnShowModel = findViewById<Button>(R.id.btnShowModel)
        val lblResponse = findViewById<AppCompatTextView>(R.id.lblResponse)
        wp7progressBar = findViewById(R.id.wp7progressBar)
        webView = findViewById(R.id.webView)


        btnClick.setOnClickListener {
            val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
            var authorization = sharedPreference.getString("token", "").toString()
            wp7progressBar.showProgressBar()

            mainActivityViewModel.getWebFormData(authorization)!!
                .observe(this, Observer { serviceSetterGetter ->
                    wp7progressBar.hideProgressBar()
                    /* supportFragmentManager.beginTransaction()
                         .replace(R.id.containerFragment, WebViewFragment()).commitAllowingStateLoss();*/

                    ///val webViewFragment = WebViewFragment(serviceSetterGetter.upload_page_url.toString())
                    // webViewFragment.show(supportFragmentManager,"asdasd");
                    //showFragment(webViewFragment)
                    webView.visibility = View.VISIBLE
                    //webView.loadUrl("https://camposha.info/");
                    val sharedPreference =
                        getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                    var editor = sharedPreference.edit()
                    editor.putString("webformcode", serviceSetterGetter.code.toString());
                    editor.commit()
                    startWebView(serviceSetterGetter.upload_page_url.toString())

                })

        }

        btnCreateAvatar.setOnClickListener {
            val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
            var webformCode = sharedPreference.getString("webformcode", "")
            var authorization = sharedPreference.getString("token", "").toString()
            lblResponse.text = "Waiting"
            webView.visibility = View.GONE
            wp7progressBar.showProgressBar()

            mainActivityViewModel.createAvatar(authorization, webformCode!!)!!
                .observe(this, Observer { createAvatarSetterGetter ->
                    wp7progressBar.hideProgressBar()
                    var editor = sharedPreference.edit()
                    editor.putString("avatar", createAvatarSetterGetter.code.toString())
                    editor.commit()

                    startTimerForCheckWebformStatu(createAvatarSetterGetter.code.toString());

                })
        }

        btnShowModel.setOnClickListener {
            val intent = Intent(this, SceneViewerActivity::class.java).apply {
                //var path = "/data/data/" + context!!.packageName.toString() + "/games/avatar"
                //putExtra("url", message)
            }
            startActivity(intent)
        }


    }

    private fun getToken() {
        var grantType = "client_credentials"
        val authorization = Credentials.basic(
            "TRxUofTIT1ipceyK7SsUK2MA623NMk23N24ho63u",
            "Zzi9P7yDiuUbtNzMmdSfLKMbynau4xOCIPQcX4hmBIceHNNS0M89KF5iaJvunpzk5wxLB41FwL4qRDWl6WbYhPCpaBZOz59Kt3gi1J6LumO3VuFKSxWoXv05DLMoxGLk"
        )
        mainActivityViewModel.postToken(authorization, grantType)!!
            .observe(this, Observer { tokenDetail ->
                val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                var editor = sharedPreference.edit()
                editor.putString(
                    "token",
                    tokenDetail.token_type.toString() + " " + tokenDetail.access_token.toString()
                )
                editor.commit()

            })
    }


    private var timer: Timer? = null
    private var isStatusCompled = false
    private fun startTimerForCheckWebformStatu(fileId: String) {
        val handler = Handler()
        val timertask: TimerTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    if (!isStatusCompled) {
                        checkAvatarStatus(
                            fileId
                        )
                    }
                }
            }
        }
        timer = Timer()
        timer!!.schedule(timertask, 0, 5000)
    }

    private fun checkAvatarStatus(fileId: String) {
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var authorization = sharedPreference.getString("token", "").toString()
        mainActivityViewModel.getAvatarStatus(authorization, fileId)!!
            .observe(this, Observer { getAvatarStatus ->
                if (getAvatarStatus.status == "Completed") {
                    timer!!.cancel()
                    isStatusCompled = true
                    wp7progressBar.hideProgressBar()
                    val sharedPreference =
                        getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                    var editor = sharedPreference.edit()
                    editor.putString("code", getAvatarStatus.code.toString())
                    editor.putString("avatar_code", getAvatarStatus.code.toString())
                    editor.commit()
                    isStatusCompled = true


                    postZipFile(
                        getAvatarStatus.code.toString(),
                        getAvatarStatus.avatar_code.toString()
                    )

                    downloadZipFile(
                        getAvatarStatus.code.toString(),
                        getAvatarStatus.avatar_code.toString()
                    )
                    val lblResponse = findViewById<AppCompatTextView>(R.id.lblResponse)
                    lblResponse.text = "Finished, please click to view"
                }
            })
    }


    private fun postZipFile(code: String, avatarCode: String) {
        if (serviceStarted) {
            return
        }
        serviceStarted = true
        var url = "https://p6gydiobjc55ooq4dllwisaxua0ftdxa.lambda-url.eu-west-2.on.aws/"
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var token = sharedPreference.getString("token", "").toString()
        var postZipSetterGetterInput = PostZipSetterGetterInput(code, avatarCode, token)


        mainActivityViewModel.postZipFile(url, postZipSetterGetterInput)!!
            .observe(this, Observer { createAvatarSetterGetter ->
                wp7progressBar.hideProgressBar()

            })

    }

    private fun downloadZipFile(code: String, avatarCode: String) {
        if (downloadStarted) {
            return
        }
        downloadStarted = true
        val filePath = File("/data/data/" + context!!.packageName.toString() + "/games")
        filePath.mkdirs()
        var zipFile =
            downloadZipCode(code, avatarCode, filePath)!!.observe(this, Observer { getStatus ->

            })
        var a = 1;

    }


    fun downloadZipCode(
        code: String,
        avatarCode: String,
        destinationFilePath: File
    ): MutableLiveData<Boolean> {
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var authorization = sharedPreference.getString("token", "").toString()

        val call = RetrofitClient.apiInterface.downloadZipFile(
            authorization,
            code,
            avatarCode
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.v("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Log.v("DEBUGZIPSTART : ", "1")
                val writeToDisk = writeToDisk(destinationFilePath, response.body()!!, code)
                Log.v("DEBUGZIPFINIS : ", "2")
            }
        })
        downloadStatus.value = true
        return downloadStatus
    }

    private fun writeToDisk(destinationFilePath: File, body: ResponseBody, code: String): Boolean {
        try {
            val mediaStorageDir = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ProfileImage"
            )

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(
                        "ProfileImage", "Oops! Failed create "
                                + "ProfileImage" + " directory"
                    )
                }
            }
            val futureStudioIconFile = File(destinationFilePath, "$code.zip")

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("TAG", "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                return true
            } catch (e: IOException) {
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        // extractFile(destinationFilePath.absolutePath + "/" + code + "/",destinationFilePath.absolutePath + "/" + code + ".zip")
                        unpackZip(destinationFilePath.absolutePath + "/" + code + ".zip")
                    },
                    3000 // value in milliseconds
                )

            }
        } catch (e: IOException) {
            return false
        }
    }

    fun unpackZip(filePath: String?): Boolean {
        val `is`: InputStream
        val zis: ZipInputStream
        try {
            val zipfile = File(filePath)
            val parentFolder = zipfile.parentFile.path
            var filename: String
            `is` = FileInputStream(filePath)
            zis = ZipInputStream(BufferedInputStream(`is`))
            var ze: ZipEntry
            val buffer = ByteArray(1024)
            var count: Int
            while (zis.nextEntry.also { ze = it } != null) {
                filename = ze.name
                if (ze.isDirectory) {
                    val fmd = File("$parentFolder/$filename")
                    fmd.mkdirs()
                    continue
                }
                val fout = FileOutputStream("$parentFolder/$filename")
                while (zis.read(buffer).also { count = it } != -1) {
                    fout.write(buffer, 0, count)
                }
                fout.close()
                zis.closeEntry()
            }
            zis.close()
        } catch (e: java.lang.Exception) {
            //  e.printStackTrace()
            return false
        }
        return true
    }

    private fun extractFile(destFilePath: String, inputPath: String) {
        val inputStream: InputStream
        inputStream = FileInputStream(inputPath)
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(4096)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    fun showFragment(fragment: WebViewFragment) {
        val fram = supportFragmentManager.beginTransaction()
        fram.replace(R.id.containerFragment, fragment)
        fram.commit()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView(url: String) {
        // Create new webview Client to show progress dialog
        // When opening a url or click on link
        // Javascript enabled on webview

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowContentAccess = true
        webView.settings.setGeolocationEnabled(true)      // life saver, do not remove
        //webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.webChromeClient = MyWebChromeClient()
        webView.webViewClient = object : WebViewClient() {

            // If you will not use this method url links are open in new browser
            // not in webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.url.toString())
                }
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                //  util._log(TAG, "onReceivedError ")
            }

            // Show loader on url load
            override fun onLoadResource(view: WebView, url: String) {
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // progressBar.visibility = View.GONE
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //      util._log(TAG, "onReceivedHttpError ${errorResponse?.statusCode}")
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                //  util._log(TAG, "onReceivedError ")
                WebViewClient.ERROR_AUTHENTICATION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /*  util._log(
                        TAG,
                        "error code: ${error.errorCode} " + request.url.toString() + " , " + error.description
                    )*/
                }
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                super.onReceivedSslError(view, handler, error)
                // util._log(TAG, "SSl error ")
            }
        }

        webView.loadUrl(url)

    }

    internal inner class MyWebChromeClient : WebChromeClient() {
        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected fun openFileChooser(uploadMsg: ValueCallback<*>, acceptType: String) {
            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
        }

        // For Lollipop 5.0+ Devices
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(
            mWebView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(null)
                uploadMessage = null
            }

            uploadMessage = filePathCallback

            val intent = fileChooserParams.createIntent()
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE)
            } catch (e: Exception) {
                uploadMessage = null
                //util.showToast(this@WebLink, "Cannot Open File Chooser")
                return false
            }

            return true
        }

        //For Android 4.1 only
        protected fun openFileChooser(
            uploadMsg: ValueCallback<Uri>,
            acceptType: String,
            capture: String
        ) {
            mUploadMessage = uploadMsg
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(
                Intent.createChooser(intent, "File Chooser"),
                FILECHOOSER_RESULTCODE
            )
        }

        protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            /* val result =
                 if (intent == null || resultCode != MainActivity.RESULT_OK) null else intent.data
             mUploadMessage!!.onReceiveValue(result)*/
            mUploadMessage = null
        } else Toast.makeText(
            context.applicationContext,
            "Failed to Upload Image",
            Toast.LENGTH_LONG
        ).show()
    }


}