/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.billing;

import com.google.api.services.actions_fulfillment.v2.model.SkuId;

public class SkuDetails {

  private String title;
  private String description;
  private SkuId skuId;
  private String formattedPrice;

  public SkuDetails(String title, String description, SkuId skuId,
                    String formattedPrice) {
    this.title = title;
    this.description = description;
    this.skuId = skuId;
    this.formattedPrice = formattedPrice;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public SkuId getSkuId() {
    return skuId;
  }

  public void setSkuId(SkuId skuId) {
    this.skuId = skuId;
  }

  public String getFormattedPrice() {
    return formattedPrice;
  }

  public void setFormattedPrice(String formattedPrice) {
    this.formattedPrice = formattedPrice;
  }

  @Override
  public String toString() {
    return getTitle();
  }
}
