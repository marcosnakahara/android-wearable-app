package me.dio.bootcamp.android.wear

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.wear.activity.ConfirmationActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import me.dio.bootcamp.android.shared.Meal
import me.dio.bootcamp.android.wear.databinding.ActivityMainBinding

class MainActivity : Activity(), GoogleApiClient.ConnectionCallbacks {

    private lateinit var binding: ActivityMainBinding
    private lateinit var client: GoogleApiClient
    private var currentMeal: Meal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addApi(Wearable.API)
            .build()
        client.connect()

        star.setOnClickListener {
            sendLike()
        }
    }

    override fun onConnected(p0: Bundle?) {
        Wearable.MessageApi.addListener(client) {
            currentMeal = Gson().fromJson(String(it.data), Meal::class.java)
            updateView()
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun updateView() {
        currentMeal?.let {
            tvMealTitle.text = it.title
            tvCalories.text = getString(R.string.number_calories, it.calories)
            tvIngredients.text = it.ingredients.joinToString(", ")
        }
    }

    private fun sendLike() {

        currentMeal?.let {
            val bytes = Gson().toJson(it.copy(favorited = true)).toByteArray()
            Wearable.DataApi.putDataItem(
                client,
                PutDataRequest.create("/liked")
                    .setData(bytes)
                    .setUrgent()
            ).setResultCallback {
                showConfirmationScreen()
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.w("Wear", "Google Api Client connecton suspended!")
    }

    private fun showConfirmationScreen() {
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(
            ConfirmationActivity.EXTRA_ANIMATION_TYPE,
            ConfirmationActivity.SUCCESS_ANIMATION
        )
        intent.putExtra(
            ConfirmationActivity.EXTRA_MESSAGE,
            getString(R.string.starred_meal)
        )
        startActivity(intent)
    }
}