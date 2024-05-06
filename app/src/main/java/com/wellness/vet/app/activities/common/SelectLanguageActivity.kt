package com.wellness.vet.app.activities.common

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivitySelectLanguageBinding
import com.wellness.vet.app.main_utils.AppSharedPreferences
import java.util.Locale

class SelectLanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectLanguageBinding

    private lateinit var appSharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@SelectLanguageActivity)
        binding.letsStartBtn.setOnClickListener {

            val radio: RadioButton = findViewById(binding.languageRadioGroup.checkedRadioButtonId)
            if (radio.text.equals(getString(R.string.english))) {
                setLocale("en")
            } else if (radio.text.equals(getString(R.string.urdu))) {
                setLocale("ur")
            }
        }
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)
        baseContext.resources.updateConfiguration(
            configuration,
            baseContext.resources.displayMetrics
        )

        appSharedPreferences.put("app_lang", language)

        goToLoginActivity()
    }

    private fun goToLoginActivity() {
        startActivity(Intent(this@SelectLanguageActivity, LoginActivity::class.java))
        finish()
    }
}