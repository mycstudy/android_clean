// Generated code from Butter Knife. Do not modify!
package com.xminnov.bu01.demo;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class BleMainActivity_ViewBinding implements Unbinder {
  private BleMainActivity target;

  @UiThread
  public BleMainActivity_ViewBinding(BleMainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public BleMainActivity_ViewBinding(BleMainActivity target, View source) {
    this.target = target;

    target.btnClear = Utils.findRequiredViewAsType(source, R.id.btn_clear, "field 'btnClear'", Button.class);
    target.tv_total = Utils.findRequiredViewAsType(source, R.id.tv_total, "field 'tv_total'", TextView.class);
    target.btnInventory = Utils.findRequiredViewAsType(source, R.id.btn_inventory, "field 'btnInventory'", Button.class);
    target.lvEpc = Utils.findRequiredViewAsType(source, R.id.listView_epc, "field 'lvEpc'", ListView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    BleMainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btnClear = null;
    target.tv_total = null;
    target.btnInventory = null;
    target.lvEpc = null;
  }
}
