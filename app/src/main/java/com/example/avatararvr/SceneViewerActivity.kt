package com.example.avatararvr

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial


class SceneViewerActivity : AppCompatActivity() {

    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_viewer)

        context = this@SceneViewerActivity

        val simple_btn = findViewById<Button>(R.id.simple_btn)

        simple_btn.setOnClickListener {
            val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
            sceneViewerIntent.data =
                Uri.parse("https://arvr.google.com/scene-viewer/1.0?file=https://projectarvr.s3.eu-west-2.amazonaws.com/avatar/model.gltf")
            sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox")
            startActivity(sceneViewerIntent)
        }
    }

    private fun createIntentUri() : Uri {
        val intentUri = Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
        val params = getIntentParams()
        params.forEach { (key, value) -> intentUri.appendQueryParameter(key, value) }
        return intentUri.build()
    }

    private fun getIntentParams() : HashMap<String, String> {

        val model_duck_rbt = findViewById<RadioButton>(R.id.model_duck_rbt)
        val title_switch = findViewById<SwitchMaterial>(R.id.title_switch)
        val link_switch = findViewById<SwitchMaterial>(R.id.link_switch)

        val ar_only_rbt = findViewById<RadioButton>(R.id.ar_only_rbt)
        val ar_preferred = findViewById<RadioButton>(R.id.ar_preferred)
        val resizable_switch = findViewById<SwitchMaterial>(R.id.resizable_switch)

        val map = HashMap<String, String> ()
        map["file"] = if (model_duck_rbt.isChecked) {
            Uri.parse("https://projectarvr.s3.eu-west-2.amazonaws.com/avatar/model.gltf")
        } else {
            "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Duck/glTF/Duck.gltf"
        }.toString()
        if (title_switch.isChecked) {
            map["title"] = "AR VR Project"
        }
        if (link_switch.isChecked) {
            map["link"] = "https://google.com"
        }
        ///data/data/com.example.avatararvr/games/avatar/model.gltf
        when {
            ar_only_rbt.isChecked ->  map["mode"] = "ar_only"
            ar_preferred.isChecked -> map["mode"] = "ar_preferred"
        }

        if (resizable_switch.isChecked) {
            map["resizable"] = "true"
        } else {
            map["resizable"] = "false"
        }
        return map
    }
}