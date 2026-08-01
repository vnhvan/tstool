package com.offline.saveeditor.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class AppScreenTest {
    @Test fun nullScreenFallsBackToHome() = assertEquals(AppScreen.HOME, ScreenState.normalize(null))
}
