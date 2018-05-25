// @flow
import { StyleSheet } from 'react-native';
import { ColorPalette, DEFAULT_FONT_FAMILY } from '../../../common';

export default StyleSheet.create({
  button: {
    // flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: ColorPalette.BG_BUTTON_SUBMIT,
    paddingLeft: 20,
    paddingRight: 20,
    paddingTop: 7,
    paddingBottom: 7,
    borderRadius: 5,
    height: 40,
  },
  text: {
    color: ColorPalette.white,
    fontSize: 18,
    fontFamily: DEFAULT_FONT_FAMILY,
    // fontWeight: 'bold',
    textAlignVertical: 'center',
    textAlign: 'center',
  },
});
