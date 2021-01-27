// Generated code from Butter Knife. Do not modify!
package com.xminnov.bu01.demo;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class BleServiceActivity_ViewBinding implements Unbinder {
  private BleServiceActivity target;

  @UiThread
  public BleServiceActivity_ViewBinding(BleServiceActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public BleServiceActivity_ViewBinding(BleServiceActivity target, View source) {
    this.target = target;

    target.swipe = Utils.findRequiredViewAsType(source, R.id.swipe, "field 'swipe'", SwipeRefreshLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    BleServiceActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.swipe = null;
  }
}
