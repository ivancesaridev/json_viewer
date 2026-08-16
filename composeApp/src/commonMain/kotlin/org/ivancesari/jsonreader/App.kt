package org.ivancesari.jsonreader

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.ivancesari.jsonreader.data.RecentFilesRepository
import org.ivancesari.jsonreader.data.createDataStore
import org.ivancesari.jsonreader.feature.editjson.EditJsonScreen
import org.ivancesari.jsonreader.feature.home.HomeScreen
import org.ivancesari.jsonreader.feature.home.HomeViewModel
import org.ivancesari.jsonreader.feature.jsondetail.JsonDetailScreen
import org.ivancesari.jsonreader.navigation.decodeFromRoute
import org.ivancesari.jsonreader.navigation.encodeForRoute
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.error_open_from_other_app
import org.ivancesari.jsonreader.resources.new_file_name
import org.ivancesari.jsonreader.theme.JsonReaderTheme
import org.ivancesari.jsonreader.util.resolveJsonFileInfo
import org.jetbrains.compose.resources.stringResource

@Serializable
object Home

@Serializable
data class JsonDetail(val filePath: String, val fileName: String, val fileSize: Long)

@Serializable
data class EditJson(val filePath: String?, val fileName: String)

@Composable
fun App(
    initialUri: String? = null,
    onUriHandled: () -> Unit = {}
) {
    JsonReaderTheme {
        val repository = remember { RecentFilesRepository(createDataStore()) }
        val navController = rememberNavController()

        var homeMessage by remember { mutableStateOf<String?>(null) }
        val openFailedMessage = stringResource(Res.string.error_open_from_other_app)
        val newFileName = stringResource(Res.string.new_file_name)

        LaunchedEffect(initialUri) {
            val uri = initialUri ?: return@LaunchedEffect

            // Resolving goes through the content resolver, so it is kept off the main thread.
            val fileInfo = withContext(Dispatchers.IO) { resolveJsonFileInfo(uri) }

            if (fileInfo == null) {
                // Previously this branch did nothing at all: opening a file JSON Viewer could not
                // read simply dropped the user on the home screen with no explanation.
                homeMessage = openFailedMessage
            } else {
                repository.addRecentFile(fileInfo)
                navController.navigate(
                    JsonDetail(
                        filePath = fileInfo.path.encodeForRoute(),
                        fileName = fileInfo.name,
                        fileSize = fileInfo.sizeInBytes
                    )
                )
            }
            onUriHandled()
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
                        navController.navigate(
                            JsonDetail(
                                filePath = file.path.encodeForRoute(),
                                fileName = file.name,
                                fileSize = file.sizeInBytes
                            )
                        )
                    },
                    onRemoveRecentFile = homeViewModel::onRemoveRecentFile,
                    onNewFileSelected = {
                        navController.navigate(EditJson(filePath = null, fileName = newFileName))
                    },
                    message = homeMessage,
                    onMessageShown = { homeMessage = null }
                )
            }

            composable<JsonDetail> { backStackEntry ->
                val detail: JsonDetail = backStackEntry.toRoute()
                JsonDetailScreen(
                    filePath = detail.filePath.decodeFromRoute(),
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
                EditJsonScreen(
                    filePath = edit.filePath?.decodeFromRoute().orEmpty(),
                    fileName = edit.fileName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
