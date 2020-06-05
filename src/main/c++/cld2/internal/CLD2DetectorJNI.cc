#include <jni.h>
#include <string.h>
#include <fcntl.h>
#include <fstream>
#include <sys/mman.h>
#include "edu_usc_languagedetectors_CLD2DetectorJNI.h"
#include "cld2_dynamic_compat.h"
#include "../public/compact_lang_det.h"
#include "../public/encodings.h"

using namespace CLD2;


JNIEXPORT jstring JNICALL Java_edu_usc_languagedetectors_CLD2DetectorJNI_detectLanguage
  (JNIEnv *env, jobject thisObj, jstring html, jstring tld){

	const char* buffer = env->GetStringUTFChars(html, NULL);
	const char* tldhint = env->GetStringUTFChars(tld, NULL);
	if (NULL == buffer || NULL == tldhint) return NULL;

	int buffer_length = strlen(buffer);
  	int flags = 0;
  	bool get_vector = false;
	ResultChunkVector resultchunkvector;

	bool is_plain_text = false; //input is html (not plain text)
	const Encoding enchint = CLD2::UNKNOWN_ENCODING;
	const Language langhint = CLD2::UNKNOWN_LANGUAGE;
	const CLDHints cldhints = {NULL, tldhint, enchint, langhint};
	Language language3[3];
	int percent3[3];
	double normalized_score3[3];
	int text_bytes;
	bool is_reliable;
	int valid_prefix_bytes;
	
  	Language lang_detected = CLD2::ExtDetectLanguageSummaryCheckUTF8(
                          buffer,
                          buffer_length,
                          is_plain_text,
                          &cldhints,
                          flags,
                          language3,
                          percent3,
                          normalized_score3,
                          get_vector ? &resultchunkvector : NULL,
                          &text_bytes,
                          &is_reliable,
                          &valid_prefix_bytes);

	env->ReleaseStringUTFChars(html, buffer);  // release resources
	env->ReleaseStringUTFChars(tld, tldhint);  // release resources

	const char* lang_name = CLD2::LanguageName(lang_detected);
	
	return env->NewStringUTF(lang_name);


}
