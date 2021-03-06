package ru.pnzgu.computergraphics

import android.os.Bundle
import android.widget.SeekBar
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.PI

class MainActivity : AppCompatActivity() {
    private lateinit var mainView: My3DView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainView = findViewById(R.id.mainView)
        val seekBarX: SeekBar = findViewById(R.id.seekBarX)
        seekBarX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mainView.rotationAngleX = (progress / 180.0 * PI - PI).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        val seekBarY: SeekBar = findViewById(R.id.seekBarY)
        seekBarY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mainView.rotationAngleY = (progress / 180.0 * PI - PI).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        val seekBarScale: SeekBar = findViewById(R.id.seekBarScale)
        seekBarScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mainView.scaling = (progress + 1).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val toggleButton: ToggleButton = findViewById(R.id.toggleButton)
        toggleButton.setOnClickListener { mainView.invisible = !mainView.invisible }
    }
}