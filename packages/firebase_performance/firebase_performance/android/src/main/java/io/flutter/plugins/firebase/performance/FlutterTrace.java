// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebase.performance;

import com.google.firebase.perf.metrics.Trace;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.Map;
import java.util.Objects;

class FlutterTrace implements MethodChannel.MethodCallHandler {
  private final FlutterFirebasePerformancePlugin plugin;
  private final Trace trace;

  FlutterTrace(FlutterFirebasePerformancePlugin plugin, final Trace trace) {
    this.plugin = plugin;
    this.trace = trace;
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    switch (call.method) {
      case "Trace#start":
        start(result);
        break;
      case "Trace#stop":
        stop(call, result);
        break;
      case "Trace#setMetric":
        setMetric(call, result);
        break;
      case "Trace#incrementMetric":
        incrementMetric(call, result);
        break;
      case "Trace#getMetric":
        getMetric(call, result);
        break;
      case "Trace#putAttribute":
        putAttribute(call, result);
        break;
      case "Trace#removeAttribute":
        removeAttribute(call, result);
        break;
      case "Trace#getAttributes":
        getAttributes(result);
        break;
      default:
        result.notImplemented();
    }
  }

  private void start(MethodChannel.Result result) {
    trace.start();
    result.success(null);
  }

  @SuppressWarnings("ConstantConditions")
  private void stop(MethodCall call, MethodChannel.Result result) {
    final Map<String, Object> attributes = Objects.requireNonNull((call.argument("attributes")));
    final Map<String, Object> metrics = Objects.requireNonNull((call.argument("metrics")));

    for (String key : attributes.keySet()) {
      String attributeValue = (String) attributes.get(key);

      trace.putAttribute(key, attributeValue);
    }

    for (String key : metrics.keySet()) {
      Integer metricValue = (Integer) metrics.get(key);

      trace.putMetric(key, metricValue);
    }

    trace.stop();

    final Integer handle = call.argument("handle");
    plugin.removeHandler(handle);

    result.success(null);
  }

  @SuppressWarnings("ConstantConditions")
  private void setMetric(MethodCall call, MethodChannel.Result result) {
    final String name = call.argument("name");
    final Number value = call.argument("value");
    trace.putMetric(name, value.longValue());

    result.success(null);
  }

  @SuppressWarnings("ConstantConditions")
  private void incrementMetric(MethodCall call, MethodChannel.Result result) {
    final String name = call.argument("name");
    final Number value = call.argument("value");
    trace.incrementMetric(name, value.longValue());

    result.success(null);
  }

  @SuppressWarnings("ConstantConditions")
  private void getMetric(MethodCall call, MethodChannel.Result result) {
    final String name = call.argument("name");

    result.success(trace.getLongMetric(name));
  }

  @SuppressWarnings("ConstantConditions")
  private void putAttribute(MethodCall call, MethodChannel.Result result) {
    final String name = call.argument("name");
    final String value = call.argument("value");

    trace.putAttribute(name, value);

    result.success(null);
  }

  @SuppressWarnings("ConstantConditions")
  private void removeAttribute(MethodCall call, MethodChannel.Result result) {
    final String name = call.argument("name");
    trace.removeAttribute(name);

    result.success(null);
  }

  private void getAttributes(MethodChannel.Result result) {
    result.success(trace.getAttributes());
  }
}
