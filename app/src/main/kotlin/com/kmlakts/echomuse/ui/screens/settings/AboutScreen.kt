/*
 * Echomuse (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.kmlakts.echomuse.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kmlakts.echomuse.LocalPlayerAwareWindowInsets
import com.kmlakts.echomuse.R
import com.kmlakts.echomuse.ui.component.IconButton
import com.kmlakts.echomuse.ui.utils.appBarScrollBehavior
import com.kmlakts.echomuse.ui.utils.backToMain
import com.kmlakts.echomuse.viewmodels.AboutDependencyLicenseUiCollection
import com.kmlakts.echomuse.viewmodels.AboutDependencyLicensesUiState
import com.kmlakts.echomuse.viewmodels.AboutDialog
import com.kmlakts.echomuse.viewmodels.AboutLinkCollection
import com.kmlakts.echomuse.viewmodels.AboutScreenEffect
import com.kmlakts.echomuse.viewmodels.AboutScreenState
import com.kmlakts.echomuse.viewmodels.AboutUiModel
import com.kmlakts.echomuse.viewmodels.AboutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = appBarScrollBehavior()

    LaunchedEffect(viewModel, uriHandler) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AboutScreenEffect.OpenUri -> uriHandler.openUri(effect.uri)
            }
        }
    }

    AboutScreenContent(
        state = state,
        scrollBehavior = scrollBehavior,
        onNavigateUp = navController::navigateUp,
        onNavigateHome = navController::backToMain,
        onOpenUri = viewModel::openUri,
        onOpenDependencyLicenses = viewModel::openDependencyLicenses,
        onDismissDialog = viewModel::dismissDialog,
        onRetryDependencyLicenses = viewModel::retryDependencyLicenses,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreenContent(
    state: AboutScreenState,
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateUp: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenUri: (String) -> Unit,
    onOpenDependencyLicenses: () -> Unit,
    onDismissDialog: () -> Unit,
    onRetryDependencyLicenses: () -> Unit,
) {
    val listState = rememberLazyListState()

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp,
                        onLongClick = onNavigateHome,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back_button_desc),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val playerAwareInsets =
            LocalPlayerAwareWindowInsets.current.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
            )

        when (state) {
            AboutScreenState.Loading -> {
                AboutLoadingContent(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .windowInsetsPadding(playerAwareInsets),
                )
            }

            AboutScreenState.Empty -> {
                AboutMessageContent(
                    message = stringResource(R.string.no_results_found),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .windowInsetsPadding(playerAwareInsets),
                )
            }

            is AboutScreenState.Error -> {
                AboutMessageContent(
                    message = stringResource(state.messageResId),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .windowInsetsPadding(playerAwareInsets),
                )
            }

            is AboutScreenState.Success -> {
                AboutSuccessContent(
                    model = state.model,
                    onOpenUri = onOpenUri,
                    onOpenDependencyLicenses = onOpenDependencyLicenses,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(playerAwareInsets),
                    contentPadding =
                        PaddingValues(
                            top = innerPadding.calculateTopPadding() + AboutSpacing.xs,
                            bottom = SettingsDimensions.ScreenBottomPadding,
                        ),
                    listState = listState,
                )
            }
        }
    }

    if (state is AboutScreenState.Success) {
        AboutFullScreenDialogs(
            model = state.model,
            onDismiss = onDismissDialog,
            onRetryDependencyLicenses = onRetryDependencyLicenses,
        )
    }
}

@Composable
private fun AboutLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun AboutMessageContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(AboutSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AboutFullScreenDialogs(
    model: AboutUiModel,
    onDismiss: () -> Unit,
    onRetryDependencyLicenses: () -> Unit,
) {
    when (model.activeDialog) {
        AboutDialog.NONE -> {
            Unit
        }

        AboutDialog.DEPENDENCY_LICENSES -> {
            AboutFullScreenDialog(
                title = stringResource(R.string.about_license),
                onDismiss = onDismiss,
            ) { modifier ->
                DependencyLicensesDialogContent(
                    state = model.dependencyLicensesState,
                    onRetry = onRetryDependencyLicenses,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun AboutFullScreenDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        androidx.compose.material3.IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = stringResource(R.string.close_dialog),
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                )
            },
        ) { innerPadding ->
            content(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun DependencyLicensesDialogContent(
    state: AboutDependencyLicensesUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        AboutDependencyLicensesUiState.Loading -> {
            DialogStatusContent(
                message = stringResource(R.string.loading),
                showRetry = false,
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        AboutDependencyLicensesUiState.Empty -> {
            DialogStatusContent(
                message = stringResource(R.string.no_results_found),
                showRetry = true,
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        is AboutDependencyLicensesUiState.Error -> {
            DialogStatusContent(
                message = stringResource(state.messageResId),
                showRetry = true,
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        is AboutDependencyLicensesUiState.Success -> {
            DependencyLicenseList(
                licenses = state.licenses,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun DialogStatusContent(
    message: String,
    showRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(AboutSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (!showRetry) {
            LoadingIndicator(modifier = Modifier.size(40.dp))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AboutSpacing.sm),
        )
        if (showRetry) {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = AboutSpacing.sm),
            ) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun DependencyLicenseList(
    licenses: AboutDependencyLicenseUiCollection,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .widthIn(max = AboutDimensions.DialogContentMaxWidth)
                    .fillMaxWidth(),
            contentPadding = AboutDimensions.DialogListPadding,
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            items(
                count = licenses.size,
                key = { index -> "${licenses[index].name}:${licenses[index].version.orEmpty()}:$index" },
                contentType = { "dependency_license" },
            ) { index ->
                val dependency = licenses[index]
                DependencyLicenseListItem(
                    name = dependency.name,
                    version = dependency.version,
                    licenses = dependency.licenses,
                    index = index,
                    itemCount = licenses.size,
                )
            }
        }
    }
}

@Composable
private fun DependencyLicenseListItem(
    name: String,
    version: String?,
    licenses: String?,
    index: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        onClick = NoOpAction,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemCount),
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp),
        colors = AboutListItemDefaults.colors(),
        leadingContent = {
            AboutLeadingIcon(iconResId = R.drawable.info)
        },
        overlineContent =
            version?.let { versionName ->
                {
                    Text(
                        text = versionName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
        supportingContent = {
            Text(
                text = licenses ?: stringResource(R.string.about_license_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        content = {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
private fun AboutSuccessContent(
    model: AboutUiModel,
    onOpenUri: (String) -> Unit,
    onOpenDependencyLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AboutSpacing.sm),
    ) {
        item(key = "identity", contentType = "about_identity") {
            AboutContentContainer {
                AboutIdentityCard(
                    model = model,
                    onOpenUri = onOpenUri,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item(key = "project_information", contentType = "about_actions") {
            AboutContentContainer {
                AboutProjectInformationSection(
                    onOpenDependencyLicenses = onOpenDependencyLicenses,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AboutContentContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SettingsDimensions.ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = AboutDimensions.ScreenContentMaxWidth)
                    .fillMaxWidth(),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutIdentityCard(
    model: AboutUiModel,
    onOpenUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= AboutDimensions.HorizontalHeroBreakpoint) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(AboutSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AboutSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AboutIdentity(
                        model = model,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f),
                    )
                    LinkChipRow(
                        links = model.primaryLinks,
                        onOpenUri = onOpenUri,
                        horizontalArrangement = Arrangement.spacedBy(AboutSpacing.xs, Alignment.End),
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(AboutSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AboutSpacing.md),
                ) {
                    AboutIdentity(
                        model = model,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    LinkChipRow(
                        links = model.primaryLinks,
                        onOpenUri = onOpenUri,
                        horizontalArrangement = Arrangement.spacedBy(AboutSpacing.xs, Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutIdentity(
    model: AboutUiModel,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(AboutSpacing.sm),
    ) {
        SurfaceAppIcon()
        Text(
            text = stringResource(model.appNameResId),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AboutSpacing.xs, horizontalAlignment),
            verticalArrangement = Arrangement.spacedBy(AboutSpacing.xs),
        ) {
            AboutMetadataBadge(text = model.versionName)
            model.buildHash?.let { buildHash ->
                AboutMetadataBadge(text = buildHash)
            }
            AboutMetadataBadge(text = model.buildVariant)
        }
    }
}

@Composable
private fun SurfaceAppIcon(modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        val iconTint = MaterialTheme.colorScheme.onPrimaryContainer
        val iconColorFilter = remember(iconTint) { ColorFilter.tint(iconTint) }
        Image(
            painter = painterResource(R.drawable.about_splash),
            contentDescription = null,
            colorFilter = iconColorFilter,
            modifier =
                Modifier
                    .padding(AboutSpacing.sm)
                    .size(64.dp),
        )
    }
}

@Composable
private fun AboutMetadataBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Badge(
        modifier = modifier.heightIn(min = 32.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkChipRow(
    links: AboutLinkCollection,
    onOpenUri: (String) -> Unit,
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.spacedBy(AboutSpacing.xs),
    ) {
        repeat(links.size) { index ->
            val link = links[index]
            val onClick = remember(link.url, onOpenUri) { { onOpenUri(link.url) } }

            AssistChip(
                onClick = onClick,
                leadingIcon = {
                    Icon(
                        painter = painterResource(link.iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(link.labelResId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun AboutProjectInformationSection(
    onOpenDependencyLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        AboutActionListItem(
            title = stringResource(R.string.about_license),
            iconResId = R.drawable.info,
            index = 0,
            itemCount = 1,
            onClick = onOpenDependencyLicenses,
        )
    }
}

@Composable
private fun AboutActionListItem(
    title: String,
    iconResId: Int,
    index: Int,
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemCount),
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
        colors = AboutListItemDefaults.colors(),
        leadingContent = {
            AboutLeadingIcon(iconResId = iconResId)
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        content = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
    )
}

@Composable
private fun AboutLeadingIcon(
    iconResId: Int,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier =
                Modifier
                    .padding(AboutSpacing.sm)
                    .size(20.dp),
        )
    }
}

private object AboutDimensions {
    val ScreenContentMaxWidth = 840.dp
    val DialogContentMaxWidth = 920.dp
    val HorizontalHeroBreakpoint = 600.dp
    val DialogListPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
}

private object AboutSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 16.dp
    val md = 24.dp
}

private object AboutListItemDefaults {
    @Composable
    fun colors() =
        ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
}

private val NoOpAction: () -> Unit = {}
