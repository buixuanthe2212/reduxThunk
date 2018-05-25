import ReactNative from 'react-native';
import I18n from 'react-native-i18n';

// Import all locales
import en from './en.json';
import ja from './ja.json';

// Should the app fallback to English if user locale doesn't exists
I18n.defaultLocale = 'ja';
I18n.fallbacks = true;

// Define the supported translations
I18n.translations = {
  en,
  ja,
};

const currentLocale = I18n.currentLocale();

// Is it a RTL language?
export const isRTL =
  currentLocale.indexOf('ja') === 0 || currentLocale.indexOf('ja') === 0;

// Allow RTL alignment in RTL languages
ReactNative.I18nManager.allowRTL(isRTL);

// The method we'll use instead of a regular string
export function strings(name) {
  return I18n.t(name);
}

export default I18n;
