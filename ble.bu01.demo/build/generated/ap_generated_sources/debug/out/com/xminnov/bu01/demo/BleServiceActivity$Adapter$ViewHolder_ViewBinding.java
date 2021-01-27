// Generated code from Butter Knife. Do not modify!
package com.xminnov.bu01.demo;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class BleServiceActivity$Adapter$ViewHolder_ViewBinding implements Unbinder {
  private BleServiceActivity.Adapter.ViewHolder target;

  @UiThread
  public BleServiceActivity$Adapter$ViewHolder_ViewBinding(BleServiceActivity.Adapter.ViewHolder target,
      View source) {
    this.target = target;

    target.name = Utils.findRequiredViewAsType(source, R.id.name, "field 'name'", TextView.class);
    target.mac = Utils.findRequiredViewAsType(source, R.id.mac, "field 'mac'", TextView.class);
    target.rssi = Utils.findRequiredViewAsType(source, R.id.rssi, "field 'rssi'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    BleServiceActivity.Adapter.ViewHolder target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.name = null;
    target.mac = null;
    target.rssi = null;
  }
}
