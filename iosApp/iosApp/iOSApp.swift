import SwiftUI
import ComposeApp
import UIKit

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    @State private var showCamera = false
    
    var body: some View {
        ZStack {
            ComposeView(showCamera: $showCamera)
                .ignoresSafeArea(.all)
            
            if showCamera {
                ImagePicker(onImageCaptured: { image in
                    if let image = image {
                        // Save image using ImageCapture
                        let imageCapture = ImageCapture()
                        _ = imageCapture.saveImageData(image: image)
                        CameraBridge.shared.reportCaptureResult(success: true)
                    } else {
                        CameraBridge.shared.cancelCapture()
                    }
                    showCamera = false
                })
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("LaunchCamera"))) { _ in
            showCamera = true
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    @Binding var showCamera: Bool
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ImagePicker: UIViewControllerRepresentable {
    let onImageCaptured: (UIImage?) -> Void
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = .camera
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(onImageCaptured: onImageCaptured)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let onImageCaptured: (UIImage?) -> Void
        
        init(onImageCaptured: @escaping (UIImage?) -> Void) {
            self.onImageCaptured = onImageCaptured
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            let image = info[.originalImage] as? UIImage
            onImageCaptured(image)
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            onImageCaptured(nil)
        }
    }
}
