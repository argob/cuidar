package ar.gob.coronavirus.flujos.pantallaprincipal.ui.pantallaprincipal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import ar.gob.coronavirus.R;
import ar.gob.coronavirus.data.UserStatus;
import ar.gob.coronavirus.flujos.autodiagnostico.resultado.ResultadoActivity;
import ar.gob.coronavirus.utils.InternetUtils;
import ar.gob.coronavirus.utils.dialogs.FullScreenDialog;
import ar.gob.coronavirus.utils.observables.Event;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoInfectadoFragment extends BaseMainFragment {

    public NoInfectadoFragment() {
        super(R.layout.fragment_no_infectado);
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpHeader(R.drawable.gradiente_azul, R.drawable.ic_no_contagioso_azul, R.string.h_description_quedate_en_casa, 30);

        TextView habilitarCirculacion = view.findViewById(R.id.qr_boton_agregar_certificado);
        habilitarCirculacion.setOnClickListener(v -> {
            if (InternetUtils.isConnected(getContext())) {
                getViewModel().habilitarCirculacion();
            } else {
                crearDialogo();
            }
        });
    }

    private void crearDialogo() {
        FullScreenDialog.newInstance(
                getString(R.string.hubo_error),
                getString(R.string.no_hay_internet),
                getString(R.string.cerrar).toUpperCase(),
                R.drawable.ic_error
        ).show(getParentFragmentManager(), "TAG");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getViewModel().obtenerLevantarWebLiveData().observe(getViewLifecycleOwner(), new Observer<Event<Intent>>() {
            @Override
            public void onChanged(Event<Intent> intentEvent) {
                if (intentEvent.getOrNull() != null) {
                    try {
                        startActivity(intentEvent.get());
                    } catch (ActivityNotFoundException exception) {
                        Toast.makeText(requireActivity(), getString(R.string.should_install_default_browser_warning), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        escucharCambiosDelUsuario();
    }

    private void escucharCambiosDelUsuario() {
        getViewModel().obtenerUltimoEstadoLiveData().observe(getViewLifecycleOwner(), userWithPermits -> {
            try {
                getViewModel().despacharEventoNavegacion();
                setUpUserInfo(userWithPermits.getUser(), getString(R.string.h_recommendation_no_contagioso));
                String fechaVencimiento = userWithPermits.getUser().getCurrentState().getExpirationDate();
                boolean isNotContagious = userWithPermits.getUser().getCurrentState().getUserStatus() == UserStatus.NOT_CONTAGIOUS;
                setUpSymptomsSection(R.string.auto_diagnostico, getString(R.string.sintomas_resultado_sin_sintomas), R.color.covid_azul, fechaVencimiento, !isNotContagious, ResultadoActivity.OpcionesNavegacion.RESULTADO_VERDE);
            } catch (Exception ignored) {
            }
        });
    }
}
