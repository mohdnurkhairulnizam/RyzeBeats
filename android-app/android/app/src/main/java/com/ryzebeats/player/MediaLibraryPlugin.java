package com.ryzebeats.player;

import android.content.Intent;
import android.net.Uri;
import android.media.MediaMetadataRetriever;
import android.util.Base64;
import androidx.core.content.FileProvider;
import java.io.File;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "MediaLibrary")
public class MediaLibraryPlugin extends Plugin {
    @PluginMethod public void artwork(PluginCall call) { String uriValue = call.getString("uri", ""); MediaMetadataRetriever retriever = new MediaMetadataRetriever(); try { retriever.setDataSource(getContext(), Uri.parse(uriValue)); byte[] image = retriever.getEmbeddedPicture(); JSObject result = new JSObject(); if (image != null) result.put("dataUrl", "data:image/jpeg;base64," + Base64.encodeToString(image, Base64.NO_WRAP)); call.resolve(result); } catch (Exception error) { call.reject("Artwork unavailable"); } finally { try { retriever.release(); } catch (Exception ignored) {} } }
    @PluginMethod public void rename(PluginCall call) { String uriValue = call.getString("uri", ""); String name = call.getString("name", ""); if (name.isEmpty()) { call.reject("A file name is required"); return; } try { android.content.ContentValues values = new android.content.ContentValues(); values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name); int updated = getContext().getContentResolver().update(Uri.parse(uriValue), values, null, null); JSObject result = new JSObject(); result.put("renamed", updated > 0); call.resolve(result); } catch (Exception error) { call.reject("Android could not rename this file", error); } }
    @PluginMethod public void share(PluginCall call) { String uriValue = call.getString("uri", ""); try { Uri source = Uri.parse(uriValue); Uri shareUri = source; if ("file".equalsIgnoreCase(source.getScheme())) shareUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", new File(source.getPath())); Intent send = new Intent(Intent.ACTION_SEND); send.setType("audio/mpeg"); send.putExtra(Intent.EXTRA_STREAM, shareUri); send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); getContext().startActivity(Intent.createChooser(send, "Share music file")); call.resolve(); } catch (Exception error) { call.reject("Android could not share this file", error); } }
    @PluginMethod public void delete(PluginCall call) { String uriValue = call.getString("uri", ""); try { int deleted = getContext().getContentResolver().delete(Uri.parse(uriValue), null, null); JSObject result = new JSObject(); result.put("deleted", deleted > 0); call.resolve(result); } catch (Exception error) { call.reject("Android could not delete this file", error); } }
}
