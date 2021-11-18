// Copyright 2021 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_performance_platform_interface/src/method_channel/method_channel_trace.dart';
import 'package:flutter/services.dart';

import '../../firebase_performance_platform_interface.dart';
import '../platform_interface/platform_interface_firebase_performance.dart';
import 'method_channel_http_metric.dart';

/// The method channel implementation of [FirebasePerformancePlatform].
class MethodChannelFirebasePerformance extends FirebasePerformancePlatform {
  MethodChannelFirebasePerformance({required FirebaseApp app})
      : _handle = _nextHandle++,
        super(appInstance: app);
  static const MethodChannel channel =
      MethodChannel('plugins.flutter.io/firebase_performance');

  /// Internal stub class initializer.
  ///
  /// When the user code calls an auth method, the real instance is
  /// then initialized via the [delegateFor] method.
  MethodChannelFirebasePerformance._()
      : _handle = 0,
        super();

  static int _nextHandle = 0;
  final int _handle;

  /// Returns a stub instance to allow the platform interface to access
  /// the class instance statically.
  static MethodChannelFirebasePerformance get instance {
    return MethodChannelFirebasePerformance._();
  }

  @override
  FirebasePerformancePlatform delegateFor({required FirebaseApp app}) {
    return MethodChannelFirebasePerformance(app: app);
  }

  @override
  Future<bool> isPerformanceCollectionEnabled() async {
    final isPerformanceCollectionEnabled = await channel.invokeMethod<bool>(
      'FirebasePerformance#isPerformanceCollectionEnabled',
      <String, Object?>{'handle': _handle},
    );
    return isPerformanceCollectionEnabled!;
  }

  @override
  Future<void> setPerformanceCollectionEnabled(bool enabled) {
    return channel.invokeMethod<void>(
      'FirebasePerformance#setPerformanceCollectionEnabled',
      <String, Object?>{'handle': _handle, 'enable': enabled},
    );
  }

  @override
  TracePlatform newTrace(String name) {
    final int handle = _nextHandle++;

    channel.invokeMethod<void>(
      'FirebasePerformance#newTrace',
      <String, Object?>{'handle': _handle, 'traceHandle': handle, 'name': name},
    );

    return MethodChannelTrace(handle, name);
  }

  @override
  HttpMetricPlatform newHttpMetric(String url, HttpMethod httpMethod) {
    final int handle = _nextHandle++;

    channel.invokeMethod<void>(
      'FirebasePerformance#newHttpMetric',
      <String, Object?>{
        'handle': _handle,
        'httpMetricHandle': handle,
        'url': url,
        'httpMethod': httpMethod.toString(),
      },
    );

    return MethodChannelHttpMetric(handle, url, httpMethod);
  }
}
