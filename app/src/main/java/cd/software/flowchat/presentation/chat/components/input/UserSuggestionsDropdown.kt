package cd.software.flowchat.presentation.chat.components.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.chat.R

/** Dropdown que muestra sugerencias de usuarios para autocompletado */
@Composable
fun UserSuggestionsDropdown(
        users: List<String>,
        query: String,
        onUserSelected: (String) -> Unit,
        onDismiss: () -> Unit
) {
    val filteredUsers =
            remember(users, query) {
                if (query.isEmpty()) users
                else {
                    users.filter { it.contains(query, ignoreCase = true) }.sortedBy {
                        !it.startsWith(query, ignoreCase = true)
                    }
                }
            }

    Card(
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
            Text(
                    stringResource(R.string.users),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(8.dp)
            )

            Divider()

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredUsers) { user ->
                    UserSuggestionItem(
                            username = user,
                            isExactMatch = user.startsWith(query, ignoreCase = true),
                            onClick = { onUserSelected(user) }
                    )
                }

                if (filteredUsers.isEmpty()) {
                    item {
                        Text(
                                stringResource(R.string.user_no_matches),
                                modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lista ordenada de sugerencias de usuarios Prioriza usuarios que empiezan con la query sobre los
 * que la contienen
 */
@Composable
fun SortedUserSuggestionsList(
        users: List<String>,
        query: String,
        onUserSelected: (String) -> Unit
) {
    val sortedUsers =
            remember(users, query) {
                if (query.isEmpty()) {
                    users
                } else {
                    val startsWithList =
                            users.filter { it.lowercase().startsWith(query.lowercase()) }
                    val containsList =
                            users.filter {
                                !it.lowercase().startsWith(query.lowercase()) &&
                                        it.lowercase().contains(query.lowercase())
                            }
                    startsWithList + containsList
                }
            }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        if (sortedUsers.isEmpty()) {
            item {
                Text(
                        text = stringResource(R.string.user_no_matches),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(sortedUsers) { username ->
                UserSuggestionItem(
                        username = username,
                        isExactMatch = username.lowercase().startsWith(query.lowercase()),
                        onClick = { onUserSelected(username) }
                )
            }
        }
    }
}

/** Item individual de sugerencia de usuario */
@Composable
fun UserSuggestionItem(username: String, isExactMatch: Boolean, onClick: () -> Unit) {
    Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            color =
                    if (isExactMatch) {
                        colorScheme.primaryContainer.copy(alpha = 0.1f)
                    } else {
                        colorScheme.surface
                    }
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = username, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
