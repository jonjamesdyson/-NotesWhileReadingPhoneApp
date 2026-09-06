package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.local.NookDatabase
import com.example.data.remote.NetworkModule
import com.example.data.repository.NookRepository
import com.example.ui.navigation.NookNavHost
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NookViewModel
import com.example.ui.viewmodel.NookViewModelFactory

class MainActivity : ComponentActivity() {

  private val viewModel: NookViewModel by viewModels {
    val database = NookDatabase.getDatabase(applicationContext)
    val repository = NookRepository(
      bookDao = database.bookDao(),
      logEntryDao = database.logEntryDao(),
      openLibraryApi = NetworkModule.openLibraryApi
    )
    NookViewModelFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        NookNavHost(
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}

