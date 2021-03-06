/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.dialogfragments;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pct.droid.R;
import pct.droid.adapters.FileArrayAdapter;
import pct.droid.adapters.models.Option;

public class FileSelectorDialogFragment extends DialogFragment {

    private static FileSelectorDialogFragment sFragment = null;
    private File currentDir;
    private FileArrayAdapter adapter;
    private ListView list;
    private Listener mListener;

    public static void show(FragmentManager fm, Listener listener) {
        if (sFragment != null)
            return;

        sFragment = new FileSelectorDialogFragment();
        sFragment.show(fm, "overlay_fragment");
        sFragment.setListener(listener);
    }

    public static void hide() {
        if (sFragment == null)
            return;

        sFragment.dismiss();
        sFragment = null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_fileselector, container);
        list = (ListView) view.findViewById(android.R.id.list);

        currentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        fill(currentDir);

        return view;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void fill(File f) {
        final File[] dirs = f.listFiles();
        getDialog().setTitle("Current dir: " + f.getName());
        final List<Option> dir = new ArrayList<>();
        List<Option> files = new ArrayList<>();

        try {
            for (File file : dirs) {
                if (file.isDirectory()) {
                    dir.add(new Option(file.getName(), "Folder", file.getAbsolutePath()));
                } else {
                    files.add(new Option(file.getName(), "File Size: " + file.length(), file.getAbsolutePath()));
                }
            }
        } catch (Exception e) {

        }

        Collections.sort(dir);
        Collections.sort(files);
        dir.addAll(files);

        if (!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new Option("..", "Parent Directory", f.getParent()));

        adapter = new FileArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, dir);

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Option o = adapter.getItem(position);

                if (o.getPath() == null) {
                    Toast.makeText(getActivity(), "Not accessible", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (o.getData().equalsIgnoreCase("folder") || o.getData().equalsIgnoreCase("parent directory")) {
                    currentDir = new File(o.getPath());
                    fill(currentDir);
                } else {
                    if (mListener != null)
                        mListener.onFileSelected(new File(o.getPath()));
                }
            }
        });
    }

    public interface Listener {
        public void onFileSelected(File f);
    }
}