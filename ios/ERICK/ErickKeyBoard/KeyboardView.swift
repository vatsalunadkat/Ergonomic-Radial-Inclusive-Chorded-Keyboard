//
//  KeyboardView.swift
//  ErickKeyBoard
//
//  Created by Starship on 2026/3/2.
//

import SwiftUI

struct KeyboardView: View {
    // 定义一个回调，用于向输入框发送文字
    var onKeyPress: (String) -> Void

    var body: some View {
        HStack(spacing: 20) {
            // 按钮 1
            Button(action: { onKeyPress("1") }) {
                Text("1")
                    .frame(maxWidth: .infinity, maxHeight: 50)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            // 按钮 2
            Button(action: { onKeyPress("2") }) {
                Text("2")
                    .frame(maxWidth: .infinity, maxHeight: 50)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
        .padding()
        .background(Color.gray.opacity(0.1)) // 模拟键盘背景色
    }
}

