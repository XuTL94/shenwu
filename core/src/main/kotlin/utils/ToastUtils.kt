import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


object ToastUtils {
    private val toastState = mutableStateOf(ToastState())

    class ToastState {
        var message by mutableStateOf("")
        var isSuccess by mutableStateOf(true)
        var isVisible by mutableStateOf(false)
    }

    fun success(message: String) {
        showToast(message, true)
    }

    fun error(message: String) {
        showToast(message, false)
    }

    private fun showToast(message: String, isSuccess: Boolean) {
        toastState.value.message = message
        toastState.value.isSuccess = isSuccess
        toastState.value.isVisible = true
    }

    @Composable
    fun ToastMessage() {
        val state = toastState.value
        if (state.isVisible) {
            Dialog(onDismissRequest = { state.isVisible = false }) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (state.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { state.isVisible = false }) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}
