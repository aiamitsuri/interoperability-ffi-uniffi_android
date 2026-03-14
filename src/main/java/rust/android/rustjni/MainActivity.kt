package rust.android.rustjni

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rust.interop.data.*
import rust.interop.logic.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // 1. Keep track of the current page number
            var currentPage by remember { mutableIntStateOf(1) }
            var resultState by remember { mutableStateOf<FilterResponse?>(null) }
            var isLoading by remember { mutableStateOf(false) }

            // 2. Automatically fetch when the page changes
            LaunchedEffect(currentPage) {
                isLoading = true
                try {
                    val params = FilterParams(
                        null,
                        null,
                        null,
                        null,
                        currentPage.toString(),
                        null
                    )
                    resultState = fetchInteroperability(params)
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    isLoading = false
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Interoperability API", style = MaterialTheme.typography.headlineMedium)
                        Text("Rust + Android Handheld", style = MaterialTheme.typography.headlineSmall)

                        // 3. Navigation Controls
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { if (currentPage > 1) currentPage-- },
                                enabled = !isLoading && currentPage > 1
                            ) { Text("Previous") }

                            Text("Page $currentPage", style = MaterialTheme.typography.bodyLarge)

                            Button(
                                onClick = { currentPage++ },
                                enabled = !isLoading && (resultState?.pagination?.totalPages?.toInt() ?: 5) > currentPage
                            ) { Text("Next") }
                        }

                        if (isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        // 4. Data List
                        resultState?.let { response ->
                            LazyColumn {
                                items(response.data) { item ->
                                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                                            Text("Language: ${item.language}", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}