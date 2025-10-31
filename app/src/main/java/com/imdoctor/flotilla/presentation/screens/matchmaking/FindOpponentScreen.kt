package com.imdoctor.flotilla.presentation.screens.matchmaking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.imdoctor.flotilla.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindOpponentScreen(onOpponentFound: (String) -> Unit, onCancel: () -> Unit) {
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.find_opponent_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.find_opponent_searching),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { isSearching = false }
                ) {
                    Text(stringResource(R.string.find_opponent_cancel_search))
                }

            } else {
                Text(
                    text = stringResource(R.string.find_opponent_ready_prompt),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { isSearching = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.find_opponent_start_search))
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.find_opponent_back_to_menu))
                }
            }


            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.find_opponent_internet_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

    }
}