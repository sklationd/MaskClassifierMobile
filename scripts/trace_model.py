import torch
import torchvision

model = torchvision.models.mobilenet_v2(num_classes=5)
model.load_state_dict(torch.load("best.pt", map_location=torch.device('cpu')))
model.eval()

example = torch.rand(1,3,224,224)
traced_script_module = torch.jit.trace(model, example)
traced_script_module.save("../app/src/main/assets/model.pt")