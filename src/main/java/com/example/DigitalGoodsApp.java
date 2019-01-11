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

package com.example;

import com.example.billing.DigitalGoodsService;
import com.example.billing.SkuDetails;
import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.Capability;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.CompletePurchase;
import com.google.actions.api.response.helperintent.DigitalPurchaseCheck;
import com.google.actions.api.response.helperintent.SelectionCarousel;
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem;
import com.google.api.services.actions_fulfillment.v2.model.Entitlement;
import com.google.api.services.actions_fulfillment.v2.model.OptionInfo;
import com.google.api.services.actions_fulfillment.v2.model.PackageEntitlement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.billing.DigitalGoodsService.PurchaseResult.*;

/**
 * Implements all intent handlers for this Action. Note that your App
 * must extend from DialogflowApp if using Dialogflow or ActionsSdkApp
 * for ActionsSDK based Actions.
 */
public class DigitalGoodsApp extends DialogflowApp {

  private static final String SKU_DETAILS_LIST_KEY = "skuDetailsList";

  private static final List<String> CONSUMABLE_IDS = Arrays.asList("gas");
  private static final String BUILD_ORDER_CONTEXT = "build-the-order";
  private static final int BUILD_ORDER_LIFETIME = 5;

  private static final Logger LOGGER = LoggerFactory.getLogger(
          DigitalGoodsApp.class);

  @ForIntent("Default Welcome Intent")
  public ActionResponse welcomeIntent(ActionRequest request) {
    LOGGER.info("Welcome Intent");
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder
            .add(new DigitalPurchaseCheck());
    return responseBuilder.build();
  }

  @ForIntent("Digital Purchase Check")
  public ActionResponse digitalPurchaseCheck(ActionRequest request) {
    LOGGER.info("Digital Purchase Check");
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    ActionResponse response;
    try {
      String checkResult = Objects.requireNonNull(request.getArgument(
              "DIGITAL_PURCHASE_CHECK_RESULT"))
              .getExtension().get("resultType").toString();
      if (checkResult.equalsIgnoreCase("CAN_PURCHASE")) {
        responseBuilder.add("Welcome to Digital Goods Sample. Would " +
                "you like to see what I have for " +
                "sale?");
        responseBuilder.addSuggestions(new String[]{"Yes", "No"});
      } else if (checkResult.equalsIgnoreCase(
              "CANNOT_PURCHASE") ||
              checkResult.equalsIgnoreCase(
                      "RESULT_TYPE_UNSPECIFIED")) {
        // User does not meet necessary conditions for completing a digital
        // purchase. This may be due to location, device or other factors.
        responseBuilder.add("You are not eligible to perform this " +
                "digital purchase.")
                .endConversation();
      }
    } catch (Exception e) {
      responseBuilder.add("There was an internal error. Please try " +
              "again later").endConversation();
    } finally {
      response = responseBuilder.build();
    }
    return response;
  }

  @ForIntent("Build the Order")
  public ActionResponse welcome(ActionRequest request) {
    LOGGER.info("Build the Order Intent");
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    ActionResponse response;
    try {
      List<SkuDetails> skuDetailsList = DigitalGoodsService
              .getSkuDetails(request.getAppRequest()
                      .getConversation().getConversationId());
      if (skuDetailsList == null || skuDetailsList.size() == 0) {
        responseBuilder.add("Oops, looks like there is nothing available. " +
                "Please try again later").endConversation();
      } else {
        responseBuilder.getConversationData().put(SKU_DETAILS_LIST_KEY,
                createSkuIdToSkuDetailsMap(skuDetailsList));
        responseBuilder.add("Great! I found the following items: " +
                buildSimpleResponse(skuDetailsList));
        boolean screenAvailable = request
                .hasCapability(Capability.SCREEN_OUTPUT.getValue());
        if (screenAvailable) {
          SelectionCarousel carousel = new SelectionCarousel()
                  .setItems(buildCarouselItems(skuDetailsList));
          responseBuilder.add(carousel);
        }
      }
    } catch (Exception e) {
      LOGGER.info("Exception in welcome " + e);
      e.printStackTrace();
      responseBuilder.add("Oops, something went wrong. Try again later")
              .endConversation();
    } finally {
      response = responseBuilder.build();
    }
    return response;
  }

  private String buildSimpleResponse(List<SkuDetails> skus) {
    List<String> s = new ArrayList<>();
    for (SkuDetails sku : skus) {
      s.add(sku.getTitle());
    }
    return String.join(",", s);
  }

  private String createSkuIdToSkuDetailsMap(List<SkuDetails> skus) {
    HashMap<String, SkuDetails> map = new HashMap<>();
    for (SkuDetails sku : skus) {
      map.put(sku.getSkuId().getId(), sku);
    }
    // needed to ensure proper serialization
    return new Gson().toJson(map);
  }

  private List<CarouselSelectCarouselItem> buildCarouselItems
          (List<SkuDetails> skuDetailsList) {
    List<CarouselSelectCarouselItem> items = new ArrayList<>();
    CarouselSelectCarouselItem item;
    for (SkuDetails sku : skuDetailsList) {
      item = new CarouselSelectCarouselItem();
      item.setTitle(sku.getTitle());
      item.setDescription(sku.getDescription());
      OptionInfo optionInfo = new OptionInfo();
      optionInfo.setKey(sku.getSkuId().getId());
      item.setOptionInfo(optionInfo);
      items.add(item);
    }
    return items;
  }

  @ForIntent("Initiate the Purchase")
  public ActionResponse initiatePurchase(ActionRequest request) {
    LOGGER.info("In Initate Purchase");
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    String selectedSkuId;
    if (request.getArgument("OPTION") != null) {
      selectedSkuId = request.getArgument("OPTION").getTextValue();
      LOGGER.info("selectedSkuId = " + selectedSkuId);
    } else {
      selectedSkuId = request.getParameter("SKU") == null ? null :
              request.getParameter("SKU").toString();
    }
    if (selectedSkuId == null) {
      responseBuilder.add("Oops, something went wrong, sorry.");
      return responseBuilder.endConversation().build();
    }
    LOGGER.info(selectedSkuId);
    String skuDetailsSerMap = (String) request.getConversationData()
            .get(SKU_DETAILS_LIST_KEY);
    LOGGER.info("skuDetailsMap = " + skuDetailsSerMap.getClass());
    // See what this line does in
    // https://google.github.io/gson/apidocs/com/google/gson/Gson.html
    // We need Gson to do serialization to convert back to the map.
    Map<String, SkuDetails> skuDetailsMap = new Gson().fromJson(skuDetailsSerMap,
            new TypeToken<HashMap<String, SkuDetails>>() {
            }.getType());

    SkuDetails selectedSku = skuDetailsMap.get(selectedSkuId);
    if (selectedSku == null) {
      responseBuilder.add("selectedSku is null");
      return responseBuilder.build();
    }
    LOGGER.info("Found selected Sku: " + selectedSku.getDescription());
    responseBuilder.getConversationData().put("purchasedItemSku", selectedSku);
    CompletePurchase purchaseHelper = new CompletePurchase()
            .setSkuId(selectedSku.getSkuId());
    responseBuilder.add("Great! Here you go.");
    responseBuilder.add(purchaseHelper);
    return responseBuilder.build();
  }

  private Entitlement findSelectedEntitlement(List<PackageEntitlement> entitlements,
                                              SkuDetails selectedSku) {
    for (PackageEntitlement entitlementGroup : entitlements) {
      for (Entitlement entitlement : entitlementGroup.getEntitlements()) {
        if (entitlement.getSku().equals(selectedSku.getSkuId().getId())) {
          return entitlement;
        }
      }
    }
    return null;
  }

  @ForIntent("Describe the Purchase Status")
  public ActionResponse handlePurchaseResponse(ActionRequest request) {
    LOGGER.info("Describe the Purchase Status intent start.");
    ResponseBuilder rb = getResponseBuilder(request);
    if (request.getArgument("COMPLETE_PURCHASE_VALUE") == null) {
      rb.add("Purchased failed. Check the logs.").endConversation();
      return rb.build();
    }

    String status = (String) request.getArgument("COMPLETE_PURCHASE_VALUE")
            .getExtension()
            .get("purchaseStatus");

    if (status.equalsIgnoreCase(String.valueOf(PURCHASE_STATUS_OK))) {
      rb.add(new ActionContext(BUILD_ORDER_CONTEXT, BUILD_ORDER_LIFETIME));
      rb.add("You've successfully purchased the item!. Would you like to " +
              "do anything else?");
      SkuDetails selectedSku = (SkuDetails) request.getConversationData()
              .get("purchasedItemSku");
      if (CONSUMABLE_IDS.contains(selectedSku.getSkuId().getId())) {

        Entitlement entitlementForSelectedSku = findSelectedEntitlement(request
                .getUser().getPackageEntitlements(), selectedSku);
        String purchaseToken = (String) entitlementForSelectedSku
                .getInAppDetails().getInAppPurchaseData().get("purchaseToken");
        try {
          DigitalGoodsService.consumePurchase(request.getAppRequest()
                  .getConversation().getConversationId(), purchaseToken);
          LOGGER.info("----- Consumed!");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else if (status.equalsIgnoreCase(String.valueOf(
            PURCHASE_STATUS_ALREADY_OWNED))) {
      rb.add("Purchase failed. You already own the item.")
              .endConversation();
    } else if (status.equalsIgnoreCase(String
            .valueOf(PURCHASE_STATUS_ITEM_UNAVAILABLE))) {
      rb.add("Purchase failed. Item is not available.").endConversation();
    } else if (status.equalsIgnoreCase(
            String.valueOf(PURCHASE_STATUS_ITEM_CHANGE_REQUESTED))) {
      rb.add(new ActionContext(BUILD_ORDER_CONTEXT, BUILD_ORDER_LIFETIME));
      rb.add("Looks like you've changed your mind. Would you like " +
              "to try again?");
    } else if (status.equalsIgnoreCase(String
            .valueOf(PURCHASE_STATUS_USER_CANCELLED))) {
      rb.add(new ActionContext(BUILD_ORDER_CONTEXT, BUILD_ORDER_LIFETIME));
      rb.add("Looks like you've cancelled the purchase. Do you still want " +
              "to try to do a purchase?");
    } else if (status.equalsIgnoreCase(String.valueOf(PURCHASE_STATUS_ERROR)) ||
            status.equalsIgnoreCase(String.valueOf(PURCHASE_STATUS_UNSPECIFIED))) {
      rb.add("Purchase failed. try again later.").endConversation();
    } else {
      rb.add("Oops, there was an internal error. Please try again later")
              .endConversation();
    }
    return rb.build();
  }
}
