package info.eliumontoyasadec.signova.ui.dashboard;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CreditsDialogFragment extends DialogFragment {

    public static CreditsDialogFragment newInstance() {
        return new CreditsDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        String message =
                "SIGNOVA\n\n" +
                        "Desarrollo:\n" +
                        "• Gael Montoya\n\n" +

                        "Equipo:\n" +
                        "• Luisa Vives\n " +
                        "• Valentina Fernandez\n " +
                        "• Andrea Shedid\n " +
                        "\nTecnologías:\n" +
                        "• Android SDK\n" +
                        "• Java\n" +
                        "• OpenAI API\n\n" +
                        "© 2025";

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Créditos")
                .setMessage(message)
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .create();
    }
}