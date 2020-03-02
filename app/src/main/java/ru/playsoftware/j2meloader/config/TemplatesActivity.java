/*
 * Copyright 2018 Nikita Shakarun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.playsoftware.j2meloader.config;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;
import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.base.BaseActivity;

public class TemplatesActivity extends BaseActivity implements EditNameDialog.Callback {

	static final int REQUEST_CODE_EDIT = 5;
	private TemplatesAdapter adapter;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_templates);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		setTitle(R.string.templates);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		ArrayList<Template> templates = TemplatesManager.getTemplatesList();
		ListView mListView = findViewById(R.id.list_view);
		TextView emptyView = findViewById(R.id.empty_view);
		mListView.setEmptyView(emptyView);
		registerForContextMenu(mListView);
		adapter = new TemplatesAdapter(this, templates);
		mListView.setAdapter(adapter);
		final String def = preferences.getString(Config.DEFAULT_TEMPLATE_KEY, null);
		if (def != null) {
			for (int i = 0, templatesSize = templates.size(); i < templatesSize; i++) {
				Template template = templates.get(i);
				if (template.getName().equals(def)) {
					adapter.setDefault(i);
					break;
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.templates, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.add:
				EditNameDialog.newInstance(getString(R.string.enter_name), -1)
						.show(getSupportFragmentManager(), "alert_create_template");
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_tempates, menu);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final Template template = adapter.getItem(info.position);
		if (!template.getConfig().exists()) {
			menu.findItem(R.id.action_context_default).setVisible(false);
			menu.findItem(R.id.action_context_edit).setVisible(false);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int index = info.position;
		final Template template = adapter.getItem(index);
		switch (item.getItemId()) {
			case R.id.action_context_default:
				preferences.edit().putString(Config.DEFAULT_TEMPLATE_KEY, template.getName()).apply();
				adapter.setDefault(index);
				return true;
			case R.id.action_context_edit:
				final Intent intent = new Intent(ConfigActivity.ACTION_EDIT_TEMPLATE,
						Uri.parse(template.getName()),
						getApplicationContext(), ConfigActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_context_rename:
				EditNameDialog.newInstance(getString(R.string.enter_new_name), index)
						.show(getSupportFragmentManager(), "alert_rename_template");
				break;
			case R.id.action_context_delete:
				template.delete();
				adapter.removeItem(index);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode != REQUEST_CODE_EDIT
				|| resultCode != RESULT_OK
				|| data == null)
			return;
		final String name = data.getDataString();
		if (name == null)
			return;
		adapter.addItem(new Template(name));
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onNameChanged(int id, String newName) {
		if (id == -1) {
			final Intent intent = new Intent(ConfigActivity.ACTION_EDIT_TEMPLATE,
					Uri.parse(newName), getApplicationContext(), ConfigActivity.class);
			startActivityForResult(intent, TemplatesActivity.REQUEST_CODE_EDIT);
			return;
		}
		adapter.getItem(id).renameTo(newName);
		adapter.notifyDataSetChanged();
	}
}
