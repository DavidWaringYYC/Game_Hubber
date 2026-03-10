package com.squirrelreserve.gamehubber

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var store: SettingsStore
    private var isBinding = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = SettingsStore(requireContext().applicationContext)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.gameHubFragment))
        toolbar.setupWithNavController(navController, appBarConfiguration)

        val group = view.findViewById<RadioGroup>(R.id.themeRadioGroup)
        val radioSystem = view.findViewById<RadioButton>(R.id.radioSystem)
        val radioLight = view.findViewById<RadioButton>(R.id.radioLight)
        val radioDark = view.findViewById<RadioButton>(R.id.radioDark)
        val dynamicSwitch = view.findViewById<SwitchMaterial>(R.id.switchDynamicColor)
        viewLifecycleOwner.lifecycleScope.launch {
            val mode = store.themeModeFlow.first()
            when (mode) {
                ThemeMode.SYSTEM -> radioSystem.isChecked = true
                ThemeMode.LIGHT -> radioLight.isChecked = true
                ThemeMode.DARK -> radioDark.isChecked = true
            }
            dynamicSwitch.isChecked = store.dynamicColorFlow.first()
            isBinding = false
        }
        group.setOnCheckedChangeListener { _, checkedId ->
            if (isBinding) return@setOnCheckedChangeListener
            viewLifecycleOwner.lifecycleScope.launch {
                val newMode = when (checkedId){
                    R.id.radioLight -> ThemeMode.LIGHT
                    R.id.radioDark -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
                store.setThemeMode(newMode)
                requireActivity().recreate()
            }
        }
        dynamicSwitch.setOnCheckedChangeListener { _, enabled ->
            if(isBinding) return@setOnCheckedChangeListener
            viewLifecycleOwner.lifecycleScope.launch {
                store.setDynamicColor(enabled)
                requireActivity().recreate()
            }
        }
    }
}