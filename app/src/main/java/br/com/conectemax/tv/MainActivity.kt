package br.com.conectemax.tv

import android.annotation.TargetApi
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.text.Normalizer
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val BrandNavy = Color(0xFF0B1D46)
private val BrandBlue = Color(0xFF1768CB)
private val BrandCyan = Color(0xFF45C8D8)
private val BrandOrange = Color(0xFFF45A0B)
private val DarkBackground = BrandNavy
private val BottomBarColor = BrandNavy
private val CounterBackground = Color(0xFFF0F6FC)
private val WatchingColor = BrandBlue
private val ConectePlayColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = BrandOrange,
    onSecondary = Color.White,
    tertiary = BrandCyan,
    background = Color(0xFFF7FAFD),
    onBackground = BrandNavy,
    surface = Color.White,
    onSurface = BrandNavy,
    error = Color(0xFFBA1A1A),
)
private const val CENTRAL_CONTRACTS_URL =
    "https://sgp.conecteinternet.com.br/api/central/contratos"
private const val REQUIRED_TV_PLAN = "Conecte TV"
private const val CHANNELS_PLAYLIST_URL = "http://138.0.212.26/hls/playlist.m3u"
private const val SESSION_KEY_ALIAS = "conecte_max_session_key"
private const val SESSION_PREFERENCES = "secure_session"

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String,
)

private data class CustomerProfile(
    val name: String,
    val email: String,
    val tvPlan: String,
)

private data class SavedCredentials(
    val document: String,
    val password: String,
)

private sealed interface ChannelLoadState {
    object Loading : ChannelLoadState
    data class Loaded(val channels: List<Channel>) : ChannelLoadState
    data class Error(val message: String) : ChannelLoadState
}

private sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}

private sealed interface AuthenticationResult {
    data class Authorized(val profile: CustomerProfile) : AuthenticationResult
    data class InvalidCredentials(val message: String) : AuthenticationResult
    data class NotEntitled(val message: String) : AuthenticationResult
    data class Unavailable(val message: String) : AuthenticationResult
}

private sealed interface SessionState {
    object Checking : SessionState
    data class LoggedOut(val message: String? = null) : SessionState
    data class Authenticated(val profile: CustomerProfile) : SessionState
    data class Blocked(val message: String) : SessionState
}

class MainActivity : ComponentActivity() {
    var isPipMode by mutableStateOf(false)
        private set

    private var pipAutoEnterEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConecteMaxTheme {
                ConecteMaxRoot()
            }
        }
    }

    fun configurePictureInPicture(enabled: Boolean) {
        pipAutoEnterEnabled = enabled
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(enabled)
                    setSeamlessResizeEnabled(true)
                }
            }
            .build()
        setPictureInPictureParams(params)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (
            Build.VERSION.SDK_INT in Build.VERSION_CODES.O until Build.VERSION_CODES.S &&
            pipAutoEnterEnabled &&
            !isInPictureInPictureMode
        ) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build(),
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
    }
}

@Composable
private fun ConecteMaxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ConectePlayColorScheme,
        content = content,
    )
}

@Composable
private fun BrandLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.conecte_play_logo),
        contentDescription = "Conecte TV",
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

@Composable
private fun PlayerBrandLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.conecte_tv_player_logo),
        contentDescription = "Conecte TV",
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
private fun ConecteMaxRoot() {
    val context = LocalContext.current
    val credentialStore = remember(context) { CredentialStore(context) }
    var sessionState: SessionState by remember { mutableStateOf(SessionState.Checking) }
    var validationAttempt by remember { mutableStateOf(0) }

    LaunchedEffect(validationAttempt) {
        sessionState = SessionState.Checking
        val credentials = withContext(Dispatchers.IO) { credentialStore.load() }
        if (credentials == null) {
            sessionState = SessionState.LoggedOut()
            return@LaunchedEffect
        }

        sessionState = when (val result = authenticateCustomer(
            cpfCnpj = credentials.document,
            password = credentials.password,
        )) {
            is AuthenticationResult.Authorized -> SessionState.Authenticated(result.profile)
            is AuthenticationResult.InvalidCredentials -> {
                withContext(Dispatchers.IO) { credentialStore.clear() }
                SessionState.LoggedOut("Sua sessão expirou. Entre novamente.")
            }
            is AuthenticationResult.NotEntitled -> SessionState.Blocked(result.message)
            is AuthenticationResult.Unavailable -> SessionState.Blocked(result.message)
        }
    }

    when (val state = sessionState) {
        SessionState.Checking -> SessionCheckingScreen()
        is SessionState.LoggedOut -> LoginScreen(
            credentialStore = credentialStore,
            initialMessage = state.message,
            onAuthenticated = { sessionState = SessionState.Authenticated(it) },
        )
        is SessionState.Authenticated -> ConecteMaxApp(
            profile = state.profile,
            onLogout = {
                credentialStore.clear()
                sessionState = SessionState.LoggedOut()
            },
        )
        is SessionState.Blocked -> SessionBlockedScreen(
            message = state.message,
            onRetry = { validationAttempt++ },
            onLogout = {
                credentialStore.clear()
                sessionState = SessionState.LoggedOut()
            },
        )
    }
}

@Composable
private fun LoginScreen(
    credentialStore: CredentialStore,
    initialMessage: String? = null,
    onAuthenticated: (CustomerProfile) -> Unit,
) {
    var cpfCnpj by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loginState: LoginUiState by remember(initialMessage) {
        mutableStateOf(
            initialMessage?.let(LoginUiState::Error) ?: LoginUiState.Idle,
        )
    }
    val scope = rememberCoroutineScope()

    fun submitLogin() {
        if (loginState == LoginUiState.Loading) return

        val document = cpfCnpj.filter(Char::isDigit)
        if (document.length !in setOf(11, 14)) {
            loginState = LoginUiState.Error("Informe um CPF ou CNPJ válido.")
            return
        }
        if (password.isBlank()) {
            loginState = LoginUiState.Error("Informe a senha da Central do Cliente.")
            return
        }

        scope.launch {
            loginState = LoginUiState.Loading
            when (val result = authenticateCustomer(document, password)) {
                is AuthenticationResult.Authorized -> {
                    withContext(Dispatchers.IO) {
                        credentialStore.save(document, password)
                    }
                    password = ""
                    onAuthenticated(result.profile)
                }
                is AuthenticationResult.InvalidCredentials -> {
                    loginState = LoginUiState.Error(result.message)
                }
                is AuthenticationResult.NotEntitled -> {
                    loginState = LoginUiState.Error(result.message)
                }
                is AuthenticationResult.Unavailable -> {
                    loginState = LoginUiState.Error(result.message)
                }
            }
        }
    }

    Scaffold(containerColor = Color(0xFFF0F6FC)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BrandLogo(
                modifier = Modifier
                    .size(width = 260.dp, height = 146.dp)
                    .clip(RoundedCornerShape(24.dp)),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Entre com os dados da Central do Cliente",
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedTextField(
                value = cpfCnpj,
                onValueChange = { value ->
                    cpfCnpj = value.filter(Char::isDigit).take(14)
                    if (loginState is LoginUiState.Error) loginState = LoginUiState.Idle
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                label = { Text("CPF ou CNPJ") },
                singleLine = true,
                enabled = loginState != LoginUiState.Loading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { value ->
                    password = value
                    if (loginState is LoginUiState.Error) loginState = LoginUiState.Idle
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                label = { Text("Senha") },
                singleLine = true,
                enabled = loginState != LoginUiState.Loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submitLogin() }),
            )
            if (loginState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (loginState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = { submitLogin() },
                enabled = loginState != LoginUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .height(52.dp),
            ) {
                if (loginState == LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("ENTRAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private suspend fun authenticateCustomer(
    cpfCnpj: String,
    password: String,
): AuthenticationResult = withContext(Dispatchers.IO) {
    val boundary = "ConecteMax-${UUID.randomUUID()}"
    val connection = (URL(CENTRAL_CONTRACTS_URL).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 15_000
        readTimeout = 30_000
        doOutput = true
        setRequestProperty("Accept", "application/json")
        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        setRequestProperty("User-Agent", "ConecteMaxTV/1.0")
    }

    try {
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.writeMultipartField(boundary, "cpfcnpj", cpfCnpj)
            writer.writeMultipartField(boundary, "senha", password)
            writer.write("--$boundary--\r\n")
        }

        val statusCode = connection.responseCode
        val responseStream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val responseBody = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            .orEmpty()

        if (statusCode !in 200..299) {
            return@withContext AuthenticationResult.Unavailable(
                "A Central retornou HTTP $statusCode. Tente novamente.",
            )
        }

        val response = JSONObject(responseBody)
        if (!response.optBoolean("auth", false)) {
            return@withContext AuthenticationResult.InvalidCredentials(
                "CPF/CNPJ ou senha inválidos.",
            )
        }

        val contracts = response.optJSONArray("contratos")
        var customerName = ""
        var customerEmail = ""
        var activeTvPlan = ""
        if (contracts != null) {
            for (index in 0 until contracts.length()) {
                val contract = contracts.optJSONObject(index) ?: continue
                val status = contract.optString("status").trim()
                val tvPlan = contract.optString("planotv").trim()
                if (customerName.isBlank()) {
                    customerName = contract.optString("razaosocial").trim()
                }
                if (customerEmail.isBlank()) {
                    customerEmail = contract.optJSONArray("emails")
                        ?.optString(0)
                        .orEmpty()
                        .trim()
                }
                if (
                    status.equals("Ativo", ignoreCase = true) &&
                    tvPlan.equals(REQUIRED_TV_PLAN, ignoreCase = true)
                ) {
                    activeTvPlan = tvPlan
                    break
                }
            }
        }

        if (activeTvPlan.isBlank()) {
            AuthenticationResult.NotEntitled(
                "Não foi encontrado um contrato ativo do plano $REQUIRED_TV_PLAN para este cliente.",
            )
        } else {
            AuthenticationResult.Authorized(
                CustomerProfile(
                    name = customerName.ifBlank { "Cliente Conecte TV" },
                    email = customerEmail,
                    tvPlan = activeTvPlan,
                ),
            )
        }
    } catch (_: java.net.SocketTimeoutException) {
        AuthenticationResult.Unavailable("A Central demorou para responder. Tente novamente.")
    } catch (_: Exception) {
        AuthenticationResult.Unavailable("Não foi possível conectar à Central do Cliente.")
    } finally {
        connection.disconnect()
    }
}

private class CredentialStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        SESSION_PREFERENCES,
        Context.MODE_PRIVATE,
    )

    fun save(document: String, password: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val plaintext = JSONObject()
            .put("document", document)
            .put("password", password)
            .toString()
            .toByteArray(Charsets.UTF_8)
        val encrypted = cipher.doFinal(plaintext)

        preferences.edit()
            .putString("ciphertext", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun load(): SavedCredentials? = runCatching {
        val encrypted = preferences.getString("ciphertext", null)
            ?.let { Base64.decode(it, Base64.NO_WRAP) }
            ?: return null
        val iv = preferences.getString("iv", null)
            ?.let { Base64.decode(it, Base64.NO_WRAP) }
            ?: return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        val json = JSONObject(String(cipher.doFinal(encrypted), Charsets.UTF_8))
        SavedCredentials(
            document = json.getString("document"),
            password = json.getString("password"),
        )
    }.getOrElse {
        clear()
        null
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(SESSION_KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore",
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    SESSION_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
        }.generateKey()
    }
}

@Composable
private fun SessionCheckingScreen() {
    SessionStatusScreen(message = "Verificando sua assinatura no SGP…") {
        CircularProgressIndicator(color = WatchingColor)
    }
}

@Composable
private fun SessionBlockedScreen(
    message: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
) {
    SessionStatusScreen(message = message) {
        Button(onClick = onRetry) { Text("Tentar novamente") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onLogout) { Text("Entrar com outra conta") }
    }
}

@Composable
private fun SessionStatusScreen(
    message: String,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F6FC))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BrandLogo(
            modifier = Modifier
                .size(width = 250.dp, height = 141.dp)
                .clip(RoundedCornerShape(24.dp)),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            color = Color(0xFF5B6472),
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        actions()
    }
}

private fun OutputStreamWriter.writeMultipartField(
    boundary: String,
    fieldName: String,
    value: String,
) {
    write("--$boundary\r\n")
    write("Content-Disposition: form-data; name=\"$fieldName\"\r\n")
    write("Content-Type: text/plain; charset=UTF-8\r\n\r\n")
    write(value)
    write("\r\n")
}

@Composable
private fun ConecteMaxApp(
    profile: CustomerProfile,
    onLogout: () -> Unit,
) {
    var reloadKey by remember { mutableStateOf(0) }
    var loadState: ChannelLoadState by remember { mutableStateOf(ChannelLoadState.Loading) }

    LaunchedEffect(reloadKey) {
        loadState = ChannelLoadState.Loading
        loadState = try {
            val channels = loadChannels(CHANNELS_PLAYLIST_URL)
            if (channels.isEmpty()) {
                ChannelLoadState.Error("A playlist não contém canais válidos.")
            } else {
                ChannelLoadState.Loaded(channels)
            }
        } catch (error: Exception) {
            ChannelLoadState.Error(
                error.message ?: "Não foi possível carregar a lista de canais.",
            )
        }
    }

    when (val state = loadState) {
        ChannelLoadState.Loading -> ChannelLoadingScreen()
        is ChannelLoadState.Loaded -> ConecteMaxHomeScreen(
            channels = state.channels,
            profile = profile,
            onLogout = onLogout,
        )
        is ChannelLoadState.Error -> ChannelErrorScreen(
            message = state.message,
            onRetry = { reloadKey++ },
        )
    }
}

private suspend fun loadChannels(playlistUrl: String): List<Channel> = withContext(Dispatchers.IO) {
    val connection = (URL(playlistUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 30_000
        instanceFollowRedirects = true
        setRequestProperty("Accept", "audio/x-mpegurl, application/x-mpegURL, */*")
        setRequestProperty("User-Agent", "ConecteMaxTV/1.0")
    }

    try {
        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            throw IOException("Servidor retornou HTTP $statusCode.")
        }
        val playlist = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        parseM3uPlaylist(playlist, playlistUrl)
    } finally {
        connection.disconnect()
    }
}

private fun parseM3uPlaylist(content: String, playlistUrl: String): List<Channel> {
    val attributeRegex = Regex("""([A-Za-z0-9_-]+)="([^"]*)"""")
    val channels = mutableListOf<Channel>()
    var pendingInfo: String? = null

    content.lineSequence().forEach { rawLine ->
        val line = rawLine.trim().removePrefix("\uFEFF")
        when {
            line.startsWith("#EXTINF", ignoreCase = true) -> pendingInfo = line
            line.isBlank() || line.startsWith("#") -> Unit
            pendingInfo != null -> {
                val info = pendingInfo.orEmpty()
                val attributes = attributeRegex.findAll(info).associate {
                    it.groupValues[1].lowercase() to it.groupValues[2].trim()
                }
                val streamUrl = resolvePlaylistUrl(playlistUrl, line)
                val displayName = info.substringAfterExtInfMetadata()
                    .ifBlank { attributes["tvg-name"].orEmpty() }
                    .ifBlank { "Canal ${channels.size + 1}" }
                val logoUrl = attributes["tvg-logo"]
                    ?.takeIf(String::isNotBlank)
                    ?.let { resolveLogoUrl(playlistUrl, it) }
                    .orEmpty()

                channels += Channel(
                    id = "${channels.size}:${attributes["tvg-id"].orEmpty()}:$streamUrl",
                    name = displayName,
                    streamUrl = streamUrl,
                    logoUrl = logoUrl,
                )
                pendingInfo = null
            }
        }
    }

    return channels
}

private fun String.substringAfterExtInfMetadata(): String {
    var insideQuotes = false
    forEachIndexed { index, character ->
        when (character) {
            '"' -> insideQuotes = !insideQuotes
            ',' -> if (!insideQuotes) return substring(index + 1).trim()
        }
    }
    return ""
}

private fun resolvePlaylistUrl(baseUrl: String, value: String): String =
    runCatching { URI(baseUrl).resolve(value.trim()).toString() }.getOrDefault(value.trim())

private fun resolveLogoUrl(playlistUrl: String, value: String): String = runCatching {
    val playlistUri = URI(playlistUrl)
    val originalLogoUri = playlistUri.resolve(value.trim())
    URI(
        playlistUri.scheme,
        originalLogoUri.userInfo,
        playlistUri.host,
        playlistUri.port,
        originalLogoUri.path,
        originalLogoUri.query,
        originalLogoUri.fragment,
    ).toString()
}.getOrDefault(value.trim())

@Composable
private fun ChannelLoadingScreen() {
    AppMessageScaffold {
        CircularProgressIndicator(color = WatchingColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Carregando canais…", color = Color(0xFF4B5563))
    }
}

@Composable
private fun ChannelErrorScreen(message: String, onRetry: () -> Unit) {
    AppMessageScaffold {
        Text(
            text = "Não foi possível carregar os canais",
            color = Color(0xFF202633),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(horizontal = 28.dp),
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onRetry) { Text("Tentar novamente") }
    }
}

@Composable
private fun AppMessageScaffold(content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        containerColor = Color.White,
        topBar = { TopBar(onCastClick = {}, onSearchClick = {}) },
        bottomBar = { BottomBar(onGridClick = {}, onMenuClick = {}) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content,
        )
    }
}

@Composable
private fun ConecteMaxHomeScreen(
    channels: List<Channel>,
    profile: CustomerProfile,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    require(channels.isNotEmpty()) { "A lista de canais não pode estar vazia." }

    var selectedChannelId by rememberSaveable { mutableStateOf(channels.first().id) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var isSettingsActive by rememberSaveable { mutableStateOf(false) }
    var backgroundPlaybackEnabled by rememberSaveable { mutableStateOf(true) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val selectedChannel = channels.firstOrNull { it.id == selectedChannelId } ?: channels.first()
    val activity = LocalContext.current.findActivity() as? MainActivity
    val isPipMode = activity?.isPipMode == true
    val isPlayerOnly = isFullscreen || isPipMode
    FullscreenEffect(isFullscreen = isFullscreen)
    PictureInPictureEffect(
        enabled = backgroundPlaybackEnabled && !isSearchActive && !isSettingsActive,
    )
    BackHandler(enabled = isFullscreen || isSearchActive || isSettingsActive) {
        if (isFullscreen) {
            isFullscreen = false
        } else if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        } else {
            isSettingsActive = false
        }
    }

    if (isSearchActive && !isFullscreen) {
        ChannelSearchScreen(
            channels = channels,
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onBack = {
                isSearchActive = false
                searchQuery = ""
            },
            onChannelClick = { channel ->
                selectedChannelId = channel.id
                isSearchActive = false
                searchQuery = ""
            },
            modifier = modifier,
        )
        return
    }

    if (isSettingsActive && !isFullscreen) {
        SettingsScreen(
            profile = profile,
            backgroundPlaybackEnabled = backgroundPlaybackEnabled,
            onBackgroundPlaybackChange = { backgroundPlaybackEnabled = it },
            onBack = { isSettingsActive = false },
            onLogout = onLogout,
            modifier = modifier,
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isPlayerOnly) Color.Black else Color.White,
        topBar = {
            if (!isPlayerOnly) {
                TopBar(
                    onCastClick = { /* Conectar à implementação do MediaRouter/Cast. */ },
                    onSearchClick = { isSearchActive = true },
                )
            }
        },
        bottomBar = {
            if (!isPlayerOnly) {
                BottomBar(
                    onGridClick = { /* Navegar para a grade principal. */ },
                    onMenuClick = { isSettingsActive = true },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            VideoPlayerContainer(
                streamUrl = selectedChannel.streamUrl,
                isFullscreen = isFullscreen,
                keepPlayingInPictureInPicture = backgroundPlaybackEnabled,
                showFullscreenButton = !isPipMode,
                onFullscreenClick = { isFullscreen = !isFullscreen },
                modifier = if (isPlayerOnly) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                },
            )
            if (!isPlayerOnly) {
                ChannelCounter(count = channels.size)
                ChannelList(
                    channels = channels,
                    selectedChannelId = selectedChannel.id,
                    onChannelClick = { selectedChannelId = it.id },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PictureInPictureEffect(enabled: Boolean) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() as? MainActivity }

    DisposableEffect(activity, enabled) {
        activity?.configurePictureInPicture(enabled)
        onDispose {
            if (enabled) activity?.configurePictureInPicture(false)
        }
    }
}

private fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase()
        .trim()

@Composable
private fun FullscreenEffect(isFullscreen: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(context) { context.findActivity() }

    DisposableEffect(activity, view, isFullscreen) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }

        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            insetsController?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            if (isFullscreen) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                insetsController?.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Activity.isInPictureInPictureModeCompat(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onCastClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
        navigationIcon = {
            IconButton(onClick = onCastClick) {
                Icon(
                    imageVector = Icons.Rounded.Cast,
                    contentDescription = "Espelhar para outro dispositivo",
                    tint = Color.White,
                )
            }
        },
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PlayerBrandLogo(
                    modifier = Modifier
                        .size(width = 152.dp, height = 42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 2.dp),
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Buscar canais",
                    tint = Color.White,
                )
            }
        },
    )
}

@Composable
private fun ChannelSearchScreen(
    channels: List<Channel>,
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onChannelClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val filteredChannels = remember(channels, query) {
        val normalizedQuery = query.normalizedForSearch()
        if (normalizedQuery.isBlank()) {
            channels
        } else {
            channels.filter { it.name.normalizedForSearch().contains(normalizedQuery) }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavy)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(58.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color(0xFF24272B),
                    modifier = Modifier.size(30.dp),
                )
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("Buscar canais", color = Color(0xFF8A8A8A)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Limpar busca",
                        tint = Color(0xFF24272B),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
        Text(
            text = "Grade Aberta",
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 26.dp, top = 24.dp, bottom = 16.dp),
        )
        if (filteredChannels.isEmpty()) {
            SearchEmptyState(query = query, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(filteredChannels, key = Channel::id) { channel ->
                    SearchChannelCard(channel = channel, onClick = { onChannelClick(channel) })
                }
            }
        }
    }
}

@Composable
private fun SearchChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubcomposeAsyncImage(
            model = channel.logoUrl,
            contentDescription = "Logotipo ${channel.name}",
            contentScale = ContentScale.Fit,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = WatchingColor,
                        strokeWidth = 2.dp,
                    )
                }
            },
            success = { SubcomposeAsyncImageContent() },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = channel.name.take(2).uppercase(),
                        color = WatchingColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            modifier = Modifier.size(width = 76.dp, height = 62.dp),
        )
        Text(
            text = channel.name,
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp),
        )
    }
}

@Composable
private fun SearchEmptyState(query: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.65f),
            modifier = Modifier.size(42.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhum canal encontrado",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Não encontramos resultados para “$query”.",
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun SettingsScreen(
    profile: CustomerProfile,
    backgroundPlaybackEnabled: Boolean,
    onBackgroundPlaybackChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            BottomBar(
                onGridClick = onBack,
                onMenuClick = {},
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(BrandNavy),
                contentAlignment = Alignment.Center,
            ) {
                BrandLogo(
                    modifier = Modifier
                        .size(width = 190.dp, height = 107.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(4.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                        .border(3.dp, BrandOrange, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = Color(0xFF969696),
                        modifier = Modifier.size(88.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = profile.name,
                    color = Color(0xFF161616),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (profile.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = profile.email,
                        color = Color(0xFF4B4B4B),
                        fontSize = 14.sp,
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = profile.tvPlan,
                    color = WatchingColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = Color(0xFFB8B8B8),
            )
            SettingsRow(
                icon = Icons.Rounded.PlayCircle,
                label = "Habilitar em segundo plano",
                onClick = { onBackgroundPlaybackChange(!backgroundPlaybackEnabled) },
            ) {
                Switch(
                    checked = backgroundPlaybackEnabled,
                    onCheckedChange = onBackgroundPlaybackChange,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 48.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFF333333),
                    modifier = Modifier.size(30.dp),
                )
                Text(
                    text = "Sair",
                    color = Color(0xFF202020),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 24.dp),
                )
            }
            Text(
                text = "Versão: 1.0",
                color = Color(0xFF4B4B4B),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 18.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF333333),
            modifier = Modifier.size(30.dp),
        )
        Text(
            text = label,
            color = Color(0xFF202020),
            fontSize = 17.sp,
            modifier = Modifier
                .padding(start = 24.dp)
                .weight(1f),
        )
        trailing()
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerContainer(
    streamUrl: String,
    isFullscreen: Boolean,
    keepPlayingInPictureInPicture: Boolean,
    showFullscreenButton: Boolean,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findActivity() }
    var player by remember(context) { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(player, streamUrl) {
        val currentPlayer = player ?: return@LaunchedEffect
        val mediaItem = MediaItem.Builder()
            .setUri(streamUrl)
            .build()
        currentPlayer.setMediaItem(mediaItem)
        currentPlayer.prepare()
        currentPlayer.playWhenReady = true
    }

    DisposableEffect(context, lifecycleOwner, keepPlayingInPictureInPicture) {
        fun initializePlayer() {
            if (player == null) {
                player = ExoPlayer.Builder(context).build()
            }
        }

        fun releasePlayer() {
            player?.release()
            player = null
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> initializePlayer()
                Lifecycle.Event.ON_RESUME -> player?.play()
                Lifecycle.Event.ON_PAUSE -> {
                    if (!(keepPlayingInPictureInPicture && activity?.isInPictureInPictureModeCompat() == true)) {
                        player?.pause()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (!(keepPlayingInPictureInPicture && activity?.isInPictureInPictureModeCompat() == true)) {
                        releasePlayer()
                    }
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            initializePlayer()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            releasePlayer()
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    // Os controles nativos (play, avanço, retrocesso e ajustes) ficam ocultos.
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                    this.player = player
                }
            },
            update = { it.player = player },
            onRelease = { playerView -> playerView.player = null },
        )
        if (showFullscreenButton) {
            IconButton(
                onClick = onFullscreenClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.58f)),
            ) {
                Icon(
                    imageVector = if (isFullscreen) {
                        Icons.Rounded.FullscreenExit
                    } else {
                        Icons.Rounded.Fullscreen
                    },
                    contentDescription = if (isFullscreen) {
                        "Sair da tela cheia"
                    } else {
                        "Assistir em tela cheia"
                    },
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun ChannelCounter(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(CounterBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$count CANAIS DISPONÍVEIS",
            color = Color(0xFF4B5563),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
fun ChannelList(
    channels: List<Channel>,
    selectedChannelId: String,
    onChannelClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        items(items = channels, key = Channel::id) { channel ->
            ChannelRow(
                channel = channel,
                isSelected = channel.id == selectedChannelId,
                onClick = { onChannelClick(channel) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color(0xFFE8EBEF),
            )
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubcomposeAsyncImage(
            model = channel.logoUrl,
            contentDescription = "Logotipo ${channel.name}",
            contentScale = ContentScale.Crop,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = WatchingColor,
                    )
                }
            },
            success = { SubcomposeAsyncImageContent() },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = channel.name.take(2).uppercase(),
                        color = WatchingColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = channel.name,
                color = Color(0xFF202633),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Assistindo",
                    color = WatchingColor,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
fun BottomBar(
    onGridClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
            .background(BottomBarColor)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.size(48.dp))
        IconButton(
            onClick = onGridClick,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(BrandOrange),
        ) {
            Icon(
                imageVector = Icons.Rounded.GridView,
                contentDescription = "Grade de canais",
                tint = Color.White,
                modifier = Modifier.size(27.dp),
            )
        }
        IconButton(onClick = onMenuClick, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Abrir menu",
                tint = Color.White,
                modifier = Modifier.size(29.dp),
            )
        }
    }
}
