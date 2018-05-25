// @flow
import { StyleSheet } from 'react-native';
import { ColorPalette } from '../../../common/colors';

export const styles = StyleSheet.create({
  container: {
    backgroundColor: ColorPalette.red,
  },
  container_item: {
    paddingLeft: 15,
    paddingRight: 15,
    height: 150,
    justifyContent: 'center',
  },
  button_create: {
    backgroundColor: ColorPalette.buttonColor,
    fontSize: 14,
    height: 40,
    color: ColorPalette.white,
    marginTop: 10,
    marginLeft: 25,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 25,
    borderRadius: 10,
  },

  view_line_top: {
    marginTop: 10,
    borderBottomColor: ColorPalette.buttonColor,
    borderBottomWidth: 0.5,
  },
  view_line_bottom: {
    borderBottomColor: ColorPalette.buttonColor,
    borderBottomWidth: 0.5,
  },

  item_view_top: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  item_view_bottom: {
    flexDirection: 'row',
    marginTop: 10,
    justifyContent: 'space-between',
  },
  text_progress: {
    backgroundColor: ColorPalette.buttonColor,
    fontSize: 12,
    color: ColorPalette.white,
    padding: 5,
  },
  text_item: {
    fontSize: 12,
    color: ColorPalette.black,
  },
  empty_view: {
    marginTop: 100,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonCreateNew: {
    backgroundColor: ColorPalette.buttonColor,
    marginTop: 10,
    marginLeft: 25,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 25,
    borderRadius: 10,
    height: 40,
  },
  textCreateNew: {
    color: ColorPalette.white,
    paddingBottom: 5,
    fontSize: 16,
  }
});
