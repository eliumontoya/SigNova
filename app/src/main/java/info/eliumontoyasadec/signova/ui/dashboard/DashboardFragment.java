package info.eliumontoyasadec.signova.ui.dashboard;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import info.eliumontoyasadec.signova.R;
import info.eliumontoyasadec.signova.bd.AppDatabase;
import info.eliumontoyasadec.signova.bd.GenerationEntity;

public class DashboardFragment extends Fragment {

    private RecyclerView rv;
    private GenerationAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        rv = root.findViewById(R.id.rvDashboard);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new GenerationAdapter(item -> {
            //  Abre modal de detalle (NO navegación)
            DetailDialogFragment dialog = DetailDialogFragment.newInstance(item.id);
            dialog.show(getParentFragmentManager(), "detail_dialog");
        });

        root.findViewById(R.id.cardCredits).setOnClickListener(v -> {
            CreditsDialogFragment.newInstance()
                    .show(getChildFragmentManager(), "CreditsDialog");
        });

        rv.setAdapter(adapter);

        loadData();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        //  por si regresas al dashboard y quieres refrescar lista
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<GenerationEntity> items =
                    AppDatabase.getInstance(requireContext())
                            .generationDao()
                            .getAll();

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> adapter.setData(items));
        }).start();
    }
}