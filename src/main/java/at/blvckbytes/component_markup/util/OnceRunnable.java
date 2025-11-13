/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

public class OnceRunnable implements Runnable {

  private final Runnable runnable;
  private boolean didExecute;

  public OnceRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void run() {
    if (didExecute)
      return;

    didExecute = true;
    runnable.run();
  }
}
