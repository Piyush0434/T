package com.example.expensemanager;

import android.view.View;

import androidx.fragment.app.Fragment;

public class BalancesheetFragment extends Fragment {
    private EditText fromDateEditText;
    private EditText toDateEditText;
    private RecyclerView balanceSheetTable;
    private BalanceSheetAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_balance_sheet, container, false);

        fromDateEditText = view.findViewById(R.id.from_date);
        toDateEditText = view.findViewById(R.id.to_date);
       // balanceSheetTable = view.findViewById(R.id.balance_sheet_table);

        fromDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(fromDateEditText);
            }
        });

        toDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(toDateEditText);
            }
        });

        adapter = new BalanceSheetAdapter(getActivity(), new ArrayList<BalanceSheetItem>());
        balanceSheetTable.setLayoutManager(new LinearLayoutManager(getActivity()));
        balanceSheetTable.setAdapter(adapter);

        return view;
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                editText.setText(date);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    public void updateBalanceSheet(List<BalanceSheetItem> items) {
        adapter.updateItems(items);
    }
}
