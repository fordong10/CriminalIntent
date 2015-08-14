package com.bignerdranch.android.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class CrimeListFragment extends ListFragment {
    private ArrayList<Crime> mCrimes;
    private boolean mSubtitleVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.crimes_title);
        mCrimes = CrimeLab.get(getActivity()).getCrimes();
        CrimeAdapter adapter = new CrimeAdapter(mCrimes);
        setListAdapter(adapter);
        setRetainInstance(true);
        mSubtitleVisible = false;
    }
    
    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {   
            if (mSubtitleVisible) {
                getActivity().getActionBar().setSubtitle(R.string.subtitle);
            }
        }

        ListView listView = (ListView)v.findViewById(android.R.id.list);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // 프로요와 진저브레드 버전에서는 플로팅 컨텍스트 메뉴를 사용한다.
            registerForContextMenu(listView);
        } else {
            // 허니콤 이상 버전에서는 컨텍스트 액션 바를 사용한다.
            listView.setChoiceMode(listView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                      long id, boolean checked) {
                    // 필요하지만 여기서는 특별히 구현할 게 없다.
                }

                // ActionMode.Callback 인터페이스의 메소드
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.crime_list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                    // 필요하지만 여기서는 특별히 구현할 게 없다.
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_crime:
                            CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
                            CrimeLab crimeLab = CrimeLab.get(getActivity());
                            for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                if (getListView().isItemChecked(i)) {
                                    crimeLab.deleteCrime(adapter.getItem(i));
                                }
                            }
                            mode.finish();
                            adapter.notifyDataSetChanged();
                            return true;
                        default:
                            return false;
                    }
                }

                public void onDestroyActionMode(ActionMode mode) {
                    // 필요하지만 여기서는 특별히 구현할 게 없다.
                }
            });
        }

        return v;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        // get the Crime from the adapter
        Crime c = ((CrimeAdapter)getListAdapter()).getItem(position);
        // start an instance of CrimePagerActivity
        Intent i = new Intent(getActivity(), CrimePagerActivity.class);
        i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
        startActivityForResult(i, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible && showSubtitle != null) {
            showSubtitle.setTitle(R.string.hide_subtitle);
        }
    }

    @TargetApi(11)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent i = new Intent(getActivity(), CrimeActivity.class);
                i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
                startActivityForResult(i, 0);
                return true;
            case R.id.menu_item_show_subtitle:
            	if (getActivity().getActionBar().getSubtitle() == null) {
                    getActivity().getActionBar().setSubtitle(R.string.subtitle);
                    mSubtitleVisible = true;
                    item.setTitle(R.string.hide_subtitle);
            	}  else {
            		getActivity().getActionBar().setSubtitle(null);
            		 mSubtitleVisible = false;
            		item.setTitle(R.string.show_subtitle);
            	}
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } 
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
        Crime crime = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(crime);
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private class CrimeAdapter extends ArrayAdapter<Crime> {
        public CrimeAdapter(ArrayList<Crime> crimes) {
            super(getActivity(), android.R.layout.simple_list_item_1, crimes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (null == convertView) {
                convertView = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_crime, null);
            }

            // configure the view for this Crime
            Crime c = getItem(position);

            TextView titleTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_titleTextView);
            titleTextView.setText(c.getTitle());
            TextView dateTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_dateTextView);
            dateTextView.setText(c.getDate().toString());
            CheckBox solvedCheckBox =
                (CheckBox)convertView.findViewById(R.id.crime_list_item_solvedCheckBox);
            solvedCheckBox.setChecked(c.isSolved());

            return convertView;
        }
    }
}

