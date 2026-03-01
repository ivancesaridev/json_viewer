package org.ivancesari.jsonreader

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.ivancesari.jsonreader.feature.home.HomeScreen
import org.ivancesari.jsonreader.feature.home.HomeViewModel
import org.ivancesari.jsonreader.theme.JsonReaderTheme
import org.ivancesari.jsonreader.data.createDataStore
import org.ivancesari.jsonreader.data.RecentFilesRepository
import androidx.compose.runtime.remember
import org.ivancesari.jsonreader.util.resolveJsonFileInfo
import androidx.compose.runtime.LaunchedEffect

@Serializable
object Home

@Serializable
data class JsonDetail(val filePath: String, val fileName: String, val fileSize: Long)

@Serializable
data class EditJson(val filePath: String, val fileName: String)

fun String.encodeForRoute(): String = this.replace("/", "::sl::").replace("?", "::q::")
fun String.decodeFromRoute(): String = this.replace("::sl::", "/").replace("::q::", "?")

@Composable
fun App(
    initialUri: String? = null,
    onUriHandled: () -> Unit = {}
) {
    val repository = remember { RecentFilesRepository(createDataStore()) }

    JsonReaderTheme {
        val navController = rememberNavController()

        // Handle incoming URI from outside the app
        LaunchedEffect(initialUri) {
            initialUri?.let { uri ->
                val fileInfo = resolveJsonFileInfo(uri)
                if (fileInfo != null) {
                    // Also save to recent files
                    // We need access to the ViewModel or Repository here
                    // Actually, let's just navigate. The Detail screen doesn't save to recents,
                    // but the HomeViewModel does.
                    navController.navigate(JsonDetail(
                        filePath = fileInfo.path.encodeForRoute(),
                        fileName = fileInfo.name,
                        fileSize = fileInfo.sizeInBytes
                    ))
                    onUriHandled()
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            composable<Home> {
                val homeViewModel: HomeViewModel = viewModel { HomeViewModel(repository) }
                val uiState by homeViewModel.uiState.collectAsState()

                HomeScreen(
                    uiState = uiState,
                    onFilePicked = homeViewModel::onFilePicked,
                    onFileSelected = { file ->
                        navController.navigate(JsonDetail(
                            filePath = file.path.encodeForRoute(),
                            fileName = file.name,
                            fileSize = file.sizeInBytes
                        ))
                    }
                )
            }
            
            composable<JsonDetail> { backStackEntry ->
                val detail: JsonDetail = backStackEntry.toRoute()
                val decodedPath = detail.filePath.decodeFromRoute()
                org.ivancesari.jsonreader.feature.jsondetail.JsonDetailScreen(
                    filePath = decodedPath,
                    fileName = detail.fileName,
                    fileSize = detail.fileSize,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { filePath, name ->
                        navController.navigate(EditJson(filePath.encodeForRoute(), name))
                    }
                )
            }

            composable<EditJson> { backStackEntry ->
                val edit: EditJson = backStackEntry.toRoute()
                val decodedPath = edit.filePath.decodeFromRoute()
                org.ivancesari.jsonreader.feature.editjson.EditJsonScreen(
                    filePath = decodedPath,
                    fileName = edit.fileName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
