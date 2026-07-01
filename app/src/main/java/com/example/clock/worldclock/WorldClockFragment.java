package com.example.clock;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WorldClockFragment extends Fragment {
    private List<CityClock> selectedCities;
    private WorldClockAdapter adapter;
    private List<CitySearchResult> masterCityList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_world_clock, container, false);

        RecyclerView rvWorldClocks = view.findViewById(R.id.rv_world_clocks);
        FloatingActionButton fabAddCity = view.findViewById(R.id.fab_add_city);

        selectedCities = new ArrayList<>();
        adapter = new WorldClockAdapter(selectedCities);
        
        rvWorldClocks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorldClocks.setAdapter(adapter);

        // Vuốt ngang để xóa
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                selectedCities.remove(position);
                adapter.notifyItemRemoved(position);
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvWorldClocks);

        // Chuẩn bị toàn bộ cơ sở dữ liệu thành phố thế giới
        prepareAllCities();

        fabAddCity.setOnClickListener(v -> showSearchCityDialog());

        return view;
    }

    private void prepareAllCities() {
        masterCityList = new ArrayList<>();
        String[] ids = TimeZone.getAvailableIDs();
        
        for (String id : ids) {
            // Lọc các ID có định dạng "Châu lục/Thành_phố" (vd: Asia/Ho_Chi_Minh)
            if (id.contains("/") && !id.startsWith("Etc") && !id.startsWith("System")) {
                String cityName = id.substring(id.lastIndexOf("/") + 1).replace("_", " ");
                String areaName = id.substring(0, id.indexOf("/"));
                
                // Chuyển đổi một số tên châu lục sang tiếng Việt cho thân thiện
                areaName = areaName.replace("Asia", "Châu Á")
                                 .replace("Europe", "Châu Âu")
                                 .replace("Africa", "Châu Phi")
                                 .replace("America", "Châu Mỹ")
                                 .replace("Australia", "Châu Úc")
                                 .replace("Pacific", "Thái Bình Dương")
                                 .replace("Atlantic", "Đại Tây Dương");

                masterCityList.add(new CitySearchResult(cityName, areaName, id));
            }
        }
        
        // Sắp xếp A-Z theo tên thành phố
        Collections.sort(masterCityList, (c1, c2) -> c1.getCityName().compareToIgnoreCase(c2.getCityName()));
    }

    private void showSearchCityDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        SearchView searchView = new SearchView(getContext());
        searchView.setQueryHint("Nhập tên thành phố (vd: Paris, Seoul...)");
        searchView.setIconifiedByDefault(false);
        
        ListView listView = new ListView(getContext());
        List<CitySearchResult> filteredResults = new ArrayList<>(masterCityList);
        ArrayAdapter<CitySearchResult> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, filteredResults);
        listView.setAdapter(arrayAdapter);

        layout.addView(searchView);
        layout.addView(listView);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Thêm thành phố thế giới")
                .setView(layout)
                .setNegativeButton("Đóng", null)
                .create();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredResults.clear();
                String query = newText.toLowerCase(Locale.getDefault());
                for (CitySearchResult city : masterCityList) {
                    if (city.getCityName().toLowerCase(Locale.getDefault()).contains(query)) {
                        filteredResults.add(city);
                    }
                }
                arrayAdapter.notifyDataSetChanged();
                return true;
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            CitySearchResult selected = filteredResults.get(position);
            selectedCities.add(new CityClock(selected.getCityName(), selected.getTimeZoneId()));
            adapter.notifyItemInserted(selectedCities.size() - 1);
            dialog.dismiss();
        });

        dialog.show();
    }
}