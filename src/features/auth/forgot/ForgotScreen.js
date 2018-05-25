// @flow
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { View, SafeAreaView, BackHandler, TextInput } from 'react-native';
import { NavigationActions } from 'react-navigation';
import { styles } from './styles';
import { UIText, UILogo, UISubmitButton } from '../../../components/UI';

type Props = {
  onBackPress: Function
};

class ForgotView extends Component<Props> {
  componentDidMount() {
    BackHandler.addEventListener('hardwareBackPress', this.onBackPress);
  }
  componentWillUnmount() {
    BackHandler.removeEventListener('hardwareBackPress', this.onBackPress);
  }
  onBackPress = () => {
    this.props.navigation.dispatch(NavigationActions.back());
  };

  render() {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.container}>
          <View style={styles.container_inner}>
            <UILogo />
            <UIText
              textStyle={styles.text_desc}
              underlineColorAndroid
              text="パスワードを再発行いたします。
ご登録されているメールアドレスを
入力してください。"
            />
            <View style={styles.view_form}>
              <UIText text="メールアドレス" textStyle={styles.text_forgot} />
              <TextInput style={styles.text_input} />
            </View>

            <View style={styles.view_buttons}>
              <UISubmitButton
                title="キャンセル"
                buttonStyle={{ backgroundColor: '#999' }}
                onPress={() => {
                  this.props.navigation.dispatch(NavigationActions.back());
                }}
              />
              <UISubmitButton title="送信" />
            </View>
          </View>
        </View>
      </SafeAreaView>
    );
  }
}

const mapStatesToProps = () => ({});

const mapDispatchToProps = () => ({});

export const ForgotScreen = connect(mapStatesToProps, mapDispatchToProps)(
  ForgotView,
);
