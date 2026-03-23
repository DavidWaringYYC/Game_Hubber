package com.squirrelreserve.gamehubber

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.squirrelreserve.gamehubber.data.GameProgressRepository
import com.squirrelreserve.gamehubber.ui.setupToolbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var store: SettingsStore
    private var isBinding = true
    private val gameRepo by lazy {
        GameProgressRepository(requireContext().applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = SettingsStore(requireContext().applicationContext)
        view.setupToolbar(findNavController())

        val group = view.findViewById<RadioGroup>(R.id.themeRadioGroup)
        val radioSystem = view.findViewById<RadioButton>(R.id.radioSystem)
        val radioLight = view.findViewById<RadioButton>(R.id.radioLight)
        val radioDark = view.findViewById<RadioButton>(R.id.radioDark)
        val dynamicSwitch = view.findViewById<SwitchMaterial>(R.id.switchDynamicColor)
        val btnResetTodayProgress = view.findViewById<MaterialButton>(R.id.btnResetToday)
        val btnResetAllProgress = view.findViewById<MaterialButton>(R.id.btnResetAllGames)

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
        btnResetTodayProgress.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Today?")
                .setMessage("Are you sure you wish to reset TODAY's progress?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset Progress"){_,_ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        gameRepo.resetToday()
                        findNavController().navigateUp()
                    }
                }
                .show()
        }
        btnResetAllProgress.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Today?")
                .setMessage("Are you sure you wish to reset ALL progress?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset Progress"){_,_ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        gameRepo.resetAllGameData()
                        findNavController().navigateUp()
                    }
                }
                .show()
        }
    }
}