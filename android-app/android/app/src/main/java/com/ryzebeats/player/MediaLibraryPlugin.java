package com.ryzebeats.player;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.Collections;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "MediaLibrary")
public class MediaLibraryPlugin extends Plugin {
    private static final int STORAGE_CONFIRMATION = 7431;
    private PluginCall pendingCall;
    private String pendingUri;
    private String pendingOperation;
    private String pendingName;

    @PluginMethod public void artwork(PluginCall call) { String uriValue = call.getString("uri", ""); MediaMetadataRetriever retriever = new MediaMetadataRetriever(); try { retriever.setDataSource(getContext(), Uri.parse(uriValue)); byte[] image = retriever.getEmbeddedPicture(); JSObject result = new JSObject(); if (image != null) result.put("dataUrl", "data:image/jpeg;base64," + Base64.encodeToString(image, Base64.NO_WRAP)); call.resolve(result); } catch (Exception error) { call.reject("Artwork unavailable"); } finally { try { retriever.release(); } catch (Exception ignored) {} } }
    @PluginMethod public void rename(PluginCall call) { String uriValue = call.getString("uri", ""); String name = call.getString("name", ""); if (name.isEmpty()) { call.reject("A file name is required"); return; } try { int updated = updateDisplayName(uriValue, name); if (updated > 0) { JSObject result = new JSObject(); result.put("renamed", true); call.resolve(result); } else requestStorageConfirmation(call, uriValue, "rename", name); } catch (SecurityException error) { requestStorageConfirmation(call, uriValue, "rename", name); } catch (Exception error) { call.reject("Android could not rename this file", error); } }
    private int updateDisplayName(String uriValue, String name) { Uri uri = Uri.parse(uriValue); ContentValues values = new ContentValues(); values.put(MediaStore.MediaColumns.DISPLAY_NAME, name); return getContext().getContentResolver().update(uri, values, null, null); }
    @PluginMethod public void delete(PluginCall call) { String uriValue = call.getString("uri", ""); try { int deleted = getContext().getContentResolver().delete(Uri.parse(uriValue), null, null); if (deleted > 0) { JSObject result = new JSObject(); result.put("deleted", true); call.resolve(result); } else requestStorageConfirmation(call, uriValue, "delete", null); } catch (SecurityException error) { requestStorageConfirmation(call, uriValue, "delete", null); } catch (Exception error) { call.reject("Android could not delete this file", error); } }
    private void requestStorageConfirmation(PluginCall call, String uriValue, String operation, String name) { if (Build.VERSION.SDK_INT < 30) { call.reject("Storage permission is required"); return; } try { PendingIntent request = "delete".equals(operation) ? MediaStore.createDeleteRequest(getContext().getContentResolver(), Collections.singletonList(Uri.parse(uriValue))) : MediaStore.createWriteRequest(getContext().getContentResolver(), Collections.singletonList(Uri.parse(uriValue))); pendingCall = call; pendingUri = uriValue; pendingOperation = operation; pendingName = name; getActivity().startIntentSenderForResult(request.getIntentSender(), STORAGE_CONFIRMATION, null, 0, 0, 0); } catch (Exception error) { call.reject("Android could not request permission for this file", error); } }
    @Override protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) { if (requestCode != STORAGE_CONFIRMATION || pendingCall == null) return; PluginCall call = pendingCall; String uri = pendingUri; String operation = pendingOperation; String name = pendingName; pendingCall = null; pendingUri = null; pendingOperation = null; pendingName = null; if (resultCode != android.app.Activity.RESULT_OK) { call.reject("The file operation was cancelled"); return; } try { JSObject result = new JSObject(); if ("delete".equals(operation)) { result.put("deleted", getContext().getContentResolver().delete(Uri.parse(uri), null, null) > 0); } else { result.put("renamed", updateDisplayName(uri, name) > 0); } call.resolve(result); } catch (Exception error) { call.reject("The file operation could not be completed", error); } }
    @PluginMethod public void share(PluginCall call) { String uriValue = call.getString("uri", ""); try { Uri source = Uri.parse(uriValue); Uri shareUri = source; if ("file".equalsIgnoreCase(source.getScheme())) shareUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", new File(source.getPath())); Intent send = new Intent(Intent.ACTION_SEND); send.setType("audio/mpeg"); send.putExtra(Intent.EXTRA_STREAM, shareUri); send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); getContext().startActivity(Intent.createChooser(send, "Share music file")); call.resolve(); } catch (Exception error) { call.reject("Android could not share this file", error); } }
}
